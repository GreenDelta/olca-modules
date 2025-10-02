package org.openlca.git.writer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.storage.file.PackInserter;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.TreeFormatter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.openlca.git.Compatibility;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.iterator.ChangeIterator;
import org.openlca.git.iterator.EntryIterator;
import org.openlca.git.model.Diff;
import org.openlca.git.model.DiffType;
import org.openlca.git.repo.OlcaRepository;
import org.openlca.git.util.BinaryResolver;
import org.openlca.git.util.GitUtil;
import org.openlca.git.util.ProgressMonitor;
import org.openlca.jsonld.LibraryLink;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CommitWriter {

	private static final Logger log = LoggerFactory.getLogger(CommitWriter.class);
	protected final OlcaRepository repo;
	protected final BinaryResolver binaryResolver;
	protected String ref = Constants.HEAD;
	protected PersonIdent committer = new PersonIdent("anonymous", "anonymous@anonymous.org");
	protected ProgressMonitor progressMonitor = ProgressMonitor.NULL;
	protected UsedFeatures usedFeatures;
	private PackInserter packInserter;
	private ObjectInserter objectInserter;

	public CommitWriter(OlcaRepository repo, BinaryResolver binaryResolver) {
		this.repo = repo;
		this.binaryResolver = binaryResolver;
	}

	public CommitWriter ref(String ref) {
		this.ref = ref != null ? ref : Constants.HEAD;
		return this;
	}

	public CommitWriter as(PersonIdent committer) {
		this.committer = committer != null ? committer : new PersonIdent("anonymous", "anonymous@anonymous.org");
		return this;
	}

	public CommitWriter with(ProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor != null ? progressMonitor : ProgressMonitor.NULL;
		return this;
	}

	protected String write(String message, List<Diff> changes, ObjectId... parentCommitIds) throws IOException {
		return write(message, ChangeIterator.of(repo, null, binaryResolver, changes), parentCommitIds);
	}

	protected String write(String message, ChangeIterator changeIterator, ObjectId... parentCommitIds)
			throws IOException {
		if (this.usedFeatures == null) {
			this.usedFeatures = UsedFeatures.of(repo, parentCommitIds);
		}
		Compatibility.checkRepositoryClientVersion(repo);
		try {
			init();
			var treeIds = getCommitTreeIds(parentCommitIds);
			var treeId = syncTree("", changeIterator, treeIds);
			if (progressMonitor.isCanceled() || treeId == null)
				return null;
			var commitId = commit(message, treeId, parentCommitIds);
			return commitId.name();
		} catch (ConversionException e) {
			log.error(e.getMessage());
			return null;
		} finally {
			cleanUp();
		}
	}

	private ObjectId[] getCommitTreeIds(ObjectId[] commitIds) throws IOException {
		if (commitIds == null || commitIds.length == 0)
			return null;
		var treeIds = new ObjectId[commitIds.length];
		for (var i = 0; i < commitIds.length; i++) {
			treeIds[i] = getCommitTreeId(commitIds[i]);
		}
		return treeIds;
	}

	private ObjectId getCommitTreeId(ObjectId commitId) throws IOException {
		if (commitId == null || ObjectId.zeroId().equals(commitId))
			return null;
		var commit = repo.parseCommit(commitId);
		if (commit == null)
			return null;
		return commit.getTree().getId();
	}

	private void init() {
		var firstCommit = repo.getHeadCommit() == null;
		packInserter = repo.getObjectDatabase().newPackInserter();
		packInserter.checkExisting(!firstCommit);
		objectInserter = repo.newObjectInserter();
	}

	private ObjectId syncTree(String prefix, ChangeIterator iterator, ObjectId[] treeIds) throws ConversionException {
		var appended = false;
		var tree = new TreeFormatter();
		try (var walk = createWalk(prefix, iterator, treeIds)) {
			var previous = "";
			var previousWasDeleted = false;
			while (walk.next()) {
				if (progressMonitor.isCanceled())
					return null;
				var name = walk.getNameString();
				if (name.equals(RepositoryInfo.FILE_NAME)) {
					appendRepositoryInfo(tree);
					continue;
				}
				if (previousWasDeleted && GitUtil.isBinDirOf(name, previous))
					continue;
				previous = name;
				previousWasDeleted = false;
				var mode = walk.getFileMode();
				ObjectId id = null;
				if (mode == FileMode.TREE) {
					id = handleTree(walk, iterator);
				} else if (mode == FileMode.REGULAR_FILE) {
					id = handleFile(walk);
				}
				if (id == null || id.equals(ObjectId.zeroId())) {
					previousWasDeleted = true;
					continue;
				}
				tree.append(name, mode, id);
				appended = true;
			}
		} catch (IOException e) {
			log.error("Error walking tree", e);
			return null;
		}
		if (!appended && !Strings.nullOrEmpty(prefix))
			return null;
		try {
			return objectInserter.insert(tree);
		} catch (IOException e) {
			log.error("Error inserting tree", e);
			return null;
		}
	}

	private TreeWalk createWalk(String prefix, ChangeIterator iterator, ObjectId[] treeIds) throws IOException {
		var walk = new TreeWalk(repo);
		if (treeIds != null) {
			for (var treeId : treeIds) {
				addTree(walk, prefix, treeId);
			}
		}
		if (iterator != null) {
			walk.addTree(iterator);
		} else {
			walk.addTree(new EmptyTreeIterator());
		}
		return walk;
	}

	private void addTree(TreeWalk walk, String prefix, ObjectId treeId)
			throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		if (treeId == null || treeId.equals(ObjectId.zeroId())) {
			walk.addTree(new EmptyTreeIterator());
		} else if (Strings.nullOrEmpty(prefix)) {
			walk.addTree(treeId);
		} else {
			walk.addTree(new CanonicalTreeParser(GitUtil.encode(prefix).getBytes(), walk.getObjectReader(), treeId));
		}
	}

	private ObjectId handleTree(TreeWalk walk, ChangeIterator iterator) throws ConversionException {
		var treeCount = walk.getTreeCount();
		var treeIds = new ObjectId[treeCount - 1];
		for (var i = 0; i < treeCount - 1; i++) {
			treeIds[i] = walk.getFileMode(i) != FileMode.MISSING ? walk.getObjectId(i) : null;
		}
		var hasChanged = walk.getFileMode(treeCount - 1) != FileMode.MISSING;
		if (hasChanged && isDeletedCategory(iterator.getEntryData()))
			return null;
		if (!hasChanged && treeCount == 2)
			return treeIds[0];
		var subIterator = hasChanged
				? iterator.createSubtreeIterator()
				: null;
		var prefix = GitUtil.decode(walk.getPathString());
		return syncTree(prefix, subIterator, treeIds);
	}

	private boolean isDeletedCategory(Diff data) {
		return data != null && data.isCategory && data.diffType == DiffType.DELETED;
	}

	private ObjectId handleFile(TreeWalk walk) throws IOException, ConversionException {
		var treeCount = walk.getTreeCount();
		if (walk.getFileMode(treeCount - 1) == FileMode.MISSING) {
			// second last commit is the remote commit in case of merge
			// if no conflict resolution change is provided keep the remote one
			for (var i = treeCount - 2; i >= 0; i--)
				if (walk.getFileMode(i) != FileMode.MISSING)
					return walk.getObjectId(i);
			return null;
		}
		var path = GitUtil.decode(walk.getPathString());
		var iterator = walk.getTree(treeCount - 1, EntryIterator.class);
		Diff change = iterator.getEntryData();
		var filePath = iterator.getEntryFilePath();
		if (change.diffType == DiffType.DELETED && matches(path, change, filePath))
			return null;
		if (filePath != null)
			return insertBlob(binaryResolver.resolve(change, filePath));
		if (!change.isCategory) {
			progressMonitor.subTask(change);
		}
		if (change.isCategory) {
			usedFeatures.emptyCategories();
		}
		var data = change.isCategory
				? new byte[0]
				: getData(change);
		if (progressMonitor.isCanceled())
			return null;
		if (data == null)
			throw new ConversionException("Could not convert data for " + path);
		var blobId = insertBlob(data);
		progressMonitor.worked(1);
		return blobId;
	}

	private void appendRepositoryInfo(TreeFormatter tree) {
		try {
			var schemaBytes = usedFeatures.createInfo(getLibraries())
					.json().toString().getBytes(StandardCharsets.UTF_8);
			var blobId = insertBlob(schemaBytes);
			if (blobId != null) {
				tree.append(RepositoryInfo.FILE_NAME, FileMode.REGULAR_FILE, blobId);
			}
		} catch (Exception e) {
			log.error("Error inserting repository info", e);
		}
	}

	private ObjectId insertBlob(byte[] blob) throws IOException {
		return packInserter.insert(Constants.OBJ_BLOB, blob);
	}

	private boolean matches(String path, Diff change, String filePath) {
		if (filePath != null)
			return GitUtil.isBinDirOrFileOf(path, change.path);
		if (change.isCategory)
			return path.equals(GitUtil.toEmptyCategoryPath(change.path));
		return path.equals(change.path);
	}

	private ObjectId commit(String message, ObjectId treeId, ObjectId... parentIds) {
		try {
			var commit = new CommitBuilder();
			commit.setAuthor(committer);
			commit.setCommitter(committer);
			commit.setMessage(message);
			commit.setEncoding(StandardCharsets.UTF_8);
			commit.setTreeId(treeId);
			if (parentIds != null) {
				for (var parentId : parentIds) {
					commit.addParentId(parentId);
				}
			}
			var commitId = objectInserter.insert(commit);
			updateRef(message, commitId);
			return commitId;
		} catch (IOException e) {
			log.error("failed to update head", e);
			return null;
		}
	}

	private void updateRef(String message, ObjectId commitId) throws IOException {
		var update = repo.updateRef(ref);
		update.setNewObjectId(commitId);
		if (!Constants.R_STASH.equals(ref)) {
			update.update();
		} else {
			update.setRefLogIdent(committer);
			update.setRefLogMessage(message, false);
			update.setForceRefLog(true);
			update.forceUpdate();
		}
	}

	protected void cleanUp() throws IOException {
		if (packInserter != null) {
			if (!progressMonitor.isCanceled()) {
				packInserter.flush();
			}
			packInserter.close();
			packInserter = null;
		}
		if (objectInserter != null) {
			if (!progressMonitor.isCanceled()) {
				objectInserter.flush();
			}
			objectInserter.close();
			objectInserter = null;
		}
		if (!progressMonitor.isCanceled())
			return;
		try {
			Git.wrap(repo).gc().setExpire(Instant.now()).call();
		} catch (GitAPIException e) {
			// ignore cleanup error
		}
	}

	protected List<LibraryLink> getLibraries() {
		return List.of();
	}

	protected abstract byte[] getData(Diff change) throws IOException;

	private class ConversionException extends Exception {

		private static final long serialVersionUID = -8087256833666343335L;

		private ConversionException(String message) {
			super(message);
		}

	}

}
