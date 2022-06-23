package org.openlca.git.writer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.internal.storage.file.PackInserter;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TreeFormatter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.openlca.git.iterator.ChangeIterator;
import org.openlca.git.iterator.EntryIterator;
import org.openlca.git.model.Change;
import org.openlca.git.model.DiffType;
import org.openlca.git.util.BinaryResolver;
import org.openlca.git.util.GitUtil;
import org.openlca.git.util.ProgressMonitor;
import org.openlca.git.util.Repositories;
import org.openlca.jsonld.PackageInfo;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO check error handling
public abstract class CommitWriter {

	private static final Logger log = LoggerFactory.getLogger(CommitWriter.class);
	protected final Repository repo;
	protected final BinaryResolver binaryResolver;
	private String ref = Constants.HEAD;
	private PersonIdent committer = new PersonIdent("anonymous", "anonymous@anonymous.org");
	private ProgressMonitor progressMonitor = ProgressMonitor.NULL;
	private PackInserter packInserter;
	private ObjectInserter objectInserter;

	public CommitWriter(Repository repo, BinaryResolver binaryResolver) {
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

	protected String write(String message, List<Change> changes, ObjectId... parentCommitIds) throws IOException {
		try {
			var previousCommit = Repositories.headCommitOf(repo);
			if (previousCommit != null && !isCurrentSchemaVersion())
				throw new IOException("Git repo is not in current schema version");
			init(previousCommit == null);
			var treeIds = getCommitTreeIds(parentCommitIds);
			var treeId = syncTree("", new ChangeIterator(binaryResolver, changes), treeIds);
			var commitId = commit(message, treeId, parentCommitIds);
			return commitId.name();
		} finally {
			close();
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

	private void init(boolean firstCommit) {
		if (repo instanceof FileRepository fileRepo) {
			packInserter = fileRepo.getObjectDatabase().newPackInserter();
			packInserter.checkExisting(!firstCommit);
		}
		objectInserter = repo.newObjectInserter();
	}

	private ObjectId syncTree(String prefix, ChangeIterator iterator, ObjectId[] treeIds) {
		boolean appended = false;
		var tree = new TreeFormatter();
		try (var walk = createWalk(prefix, iterator, treeIds)) {
			while (walk.next()) {
				var name = walk.getNameString();
				if (name.equals(PackageInfo.FILE_NAME))
					continue;
				var mode = walk.getFileMode();
				ObjectId id = null;
				if (mode == FileMode.TREE) {
					id = handleTree(walk, iterator);
				} else if (mode == FileMode.REGULAR_FILE) {
					id = handleFile(walk);
				}
				if (id == null || id.equals(ObjectId.zeroId()))
					continue;
				tree.append(name, mode, id);
				appended = true;
			}
		} catch (Exception e) {
			log.error("Error walking tree", e);
		}
		if (!appended && !Strings.nullOrEmpty(prefix)) {
			removed(prefix);
			return null;
		}
		if (Strings.nullOrEmpty(prefix)) {
			appendPackageInfo(tree);
		}
		try {
			var newId = objectInserter.insert(tree);
			inserted(prefix, newId);
			return newId;
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

	private ObjectId handleTree(TreeWalk walk, ChangeIterator iterator) {
		var treeCount = walk.getTreeCount();
		var treeIds = new ObjectId[treeCount - 1];
		for (var i = 0; i < treeCount - 1; i++) {
			treeIds[i] = walk.getFileMode(0) != FileMode.MISSING ? walk.getObjectId(0) : null;
		}
		var subIterator = walk.getFileMode(treeCount - 1) != FileMode.MISSING ? iterator.createSubtreeIterator() : null;
		if (subIterator != null) {
			var prefix = GitUtil.decode(walk.getPathString());
			return syncTree(prefix, subIterator, treeIds);
		}
		for (var i = treeCount - 2; i >= 0; i--)
			if (treeIds[i] != null)
				return treeIds[i];
		return null;
	}

	private ObjectId handleFile(TreeWalk walk)
			throws IOException, InterruptedException {
		var treeCount = walk.getTreeCount();
		if (walk.getFileMode(treeCount - 1) == FileMode.MISSING) {
			for (var i = treeCount - 2; i >= 0; i--)
				if (walk.getFileMode(i) != FileMode.MISSING)
					return walk.getObjectId(i);
			return null;
		}
		var path = GitUtil.decode(walk.getPathString());
		var iterator = walk.getTree(treeCount - 1, EntryIterator.class);
		Change change = iterator.getEntryData();
		var filePath = iterator.getEntryFilePath();
		if (change.diffType == DiffType.DELETED && matches(path, change, filePath)) {
			if (filePath == null) {
				removed(path);
			}
			return null;
		}
		if (filePath != null)
			return insertBlob(binaryResolver.resolve(change, filePath));
		progressMonitor.subTask("Writing", change);
		var data = getData(change);
		if (data == null)
			return null;
		var blobId = insertBlob(data);
		inserted(path, blobId);
		progressMonitor.worked(1);
		return blobId;
	}

	private void appendPackageInfo(TreeFormatter tree) {
		try {
			var schemaBytes = PackageInfo.create()
					.withLibraries(getLibraries())
					.json().toString().getBytes(StandardCharsets.UTF_8);
			var blobId = insertBlob(schemaBytes);
			if (blobId != null) {
				tree.append(PackageInfo.FILE_NAME, FileMode.REGULAR_FILE, blobId);
			}
		} catch (Exception e) {
			log.error("Error inserting package info", e);
		}
	}

	private ObjectId insertBlob(byte[] blob) throws IOException {
		if (packInserter != null)
			return packInserter.insert(Constants.OBJ_BLOB, blob);
		return objectInserter.insert(Constants.OBJ_BLOB, blob);
	}

	private boolean matches(String path, Change change, String filePath) {
		if (change == null)
			return false;
		if (filePath == null)
			return path.equals(change.path);
		return path.startsWith(change.path.substring(0, change.path.lastIndexOf(GitUtil.DATASET_SUFFIX)));
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

	private boolean isCurrentSchemaVersion() {
		var info = Repositories.infoOf(repo);
		if (info == null)
			return false;
		var schema = info.schemaVersion();
		if (schema == null)
			return false;
		return schema.isCurrent();
	}

	protected void close() throws IOException {
		if (packInserter != null) {
			packInserter.flush();
			packInserter.close();
			packInserter = null;
		}
		if (objectInserter != null) {
			objectInserter.flush();
			objectInserter.close();
			objectInserter = null;
		}
	}

	protected void inserted(String path, ObjectId id) {
	}

	protected void removed(String path) {
	}

	protected Set<String> getLibraries() {
		return null;
	}

	protected abstract byte[] getData(Change change) throws IOException;

}
