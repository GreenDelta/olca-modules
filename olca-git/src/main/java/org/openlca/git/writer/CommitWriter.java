package org.openlca.git.writer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import org.openlca.core.database.IDatabase;
import org.openlca.git.ObjectIdStore;
import org.openlca.git.iterator.ChangeIterator;
import org.openlca.git.model.Change;
import org.openlca.git.model.DiffType;
import org.openlca.git.util.GitUtil;
import org.openlca.git.util.ProgressMonitor;
import org.openlca.git.util.Repositories;
import org.openlca.jsonld.PackageInfo;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO check error handling
public class CommitWriter {

	private static final Logger log = LoggerFactory.getLogger(CommitWriter.class);
	private final Repository repo;
	private final IDatabase database;
	private String ref = Constants.HEAD;
	private PersonIdent committer = new PersonIdent("anonymous", "anonymous@anonymous.org");
	private ObjectIdStore idStore;
	private ProgressMonitor progressMonitor = ProgressMonitor.NULL;
	private String localCommitId;
	private String remoteCommitId;
	private PackInserter packInserter;
	private ObjectInserter objectInserter;
	private Converter converter;
	private ExecutorService threads;

	public CommitWriter(Repository repo, IDatabase database) {
		this.repo = repo;
		this.database = database;
	}

	public CommitWriter ref(String ref) {
		this.ref = ref != null ? ref : Constants.HEAD;
		return this;
	}

	public CommitWriter as(PersonIdent committer) {
		this.committer = committer != null ? committer : new PersonIdent("anonymous", "anonymous@anonymous.org");
		return this;
	}

	public CommitWriter saveIdsIn(ObjectIdStore idStore) {
		this.idStore = idStore;
		return this;
	}

	public CommitWriter merge(String localCommitId, String remoteCommitId) {
		this.localCommitId = localCommitId;
		this.remoteCommitId = remoteCommitId;
		return this;
	}

	public CommitWriter with(ProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor != null ? progressMonitor : ProgressMonitor.NULL;
		return this;
	}

	public String write(String message, List<Change> changes) throws IOException {
		try {
			var previousCommit = Repositories.headCommitOf(repo);
			if (previousCommit != null && !isCurrentSchemaVersion())
				throw new IOException("Git repo is not in current schema version");
			if (changes.isEmpty() && (previousCommit == null || localCommitId == null || remoteCommitId == null))
				return null;
			init(changes, previousCommit == null);
			if (localCommitId == null && previousCommit != null) {
				localCommitId = previousCommit.getId().getName();
			}
			var localTreeId = getCommitTreeId(localCommitId);
			var remoteTreeId = getCommitTreeId(remoteCommitId);
			var treeId = syncTree("", new ChangeIterator(database, changes), localTreeId, remoteTreeId);
			if (idStore != null) {
				idStore.save();
			}
			var commitId = commit(message, treeId);
			return commitId.name();
		} finally {
			close();
		}
	}

	private void init(List<Change> changes, boolean firstCommit) {
		threads = Executors.newCachedThreadPool();
		if (repo instanceof FileRepository fileRepo) {
			packInserter = fileRepo.getObjectDatabase().newPackInserter();
			packInserter.checkExisting(!firstCommit);
		}
		objectInserter = repo.newObjectInserter();
		converter = new Converter(database, threads);
		converter.start(changes.stream()
				.filter(d -> d.diffType != DiffType.DELETED)
				.sorted()
				.toList());
	}

	private ObjectId getCommitTreeId(String commitId) throws IOException {
		if (Strings.nullOrEmpty(commitId))
			return null;
		var commit = repo.parseCommit(ObjectId.fromString(commitId));
		if (commit == null)
			return null;
		return commit.getTree().getId();
	}

	private ObjectId syncTree(String prefix, ChangeIterator diffIterator, ObjectId localTreeId,
			ObjectId remoteTreeId) {
		boolean appended = false;
		var tree = new TreeFormatter();
		try (var walk = createWalk(prefix, diffIterator, localTreeId, remoteTreeId)) {
			while (walk.next()) {
				var name = walk.getNameString();
				if (name.equals(PackageInfo.FILE_NAME))
					continue;
				var mode = walk.getFileMode();
				ObjectId id = null;
				if (mode == FileMode.TREE) {
					id = handleTree(walk, diffIterator);
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
			if (idStore != null) {
				idStore.remove(prefix);
			}
			return null;
		}
		if (Strings.nullOrEmpty(prefix)) {
			appendPackageInfo(tree);
		}
		try {
			var newId = objectInserter.insert(tree);
			if (idStore != null) {
				idStore.put(prefix, newId);
			}
			return newId;
		} catch (IOException e) {
			log.error("Error inserting tree", e);
			return null;
		}
	}

	private TreeWalk createWalk(String prefix, ChangeIterator diffIterator, ObjectId localTreeId,
			ObjectId remoteTreeId) throws IOException {
		var walk = new TreeWalk(repo);
		addTree(walk, prefix, localTreeId);
		addTree(walk, prefix, remoteTreeId);
		if (diffIterator != null) {
			walk.addTree(diffIterator);
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

	private ObjectId handleTree(TreeWalk walk, ChangeIterator diffIterator) {
		var localTreeId = walk.getFileMode(0) != FileMode.MISSING ? walk.getObjectId(0) : null;
		var remoteTreeId = walk.getFileMode(1) != FileMode.MISSING ? walk.getObjectId(1) : null;
		var iterator = walk.getFileMode(2) != FileMode.MISSING ? diffIterator.createSubtreeIterator() : null;
		if (iterator == null && localTreeId == null)
			return remoteTreeId;
		if (iterator == null && remoteTreeId == null)
			return localTreeId;
		var prefix = GitUtil.decode(walk.getPathString());
		return syncTree(prefix, iterator, localTreeId, remoteTreeId);
	}

	private ObjectId handleFile(TreeWalk walk)
			throws IOException, InterruptedException {
		var localBlobId = walk.getFileMode(0) != FileMode.MISSING ? walk.getObjectId(0) : null;
		var remoteBlobId = walk.getFileMode(1) != FileMode.MISSING ? walk.getObjectId(1) : null;
		if (walk.getFileMode(2) == FileMode.MISSING)
			return remoteBlobId != null ? remoteBlobId : localBlobId;
		var path = GitUtil.decode(walk.getPathString());
		var iterator = walk.getTree(2, ChangeIterator.class);
		Change change = iterator.getEntryData();
		var file = iterator.getEntryFile();
		if (change.diffType == DiffType.DELETED && matches(path, change, file)) {
			if (file == null && idStore != null) {
				idStore.remove(path);
			}
			return null;
		}
		if (file != null)
			return insertBlob(Files.readAllBytes(file.toPath()));
		progressMonitor.subTask("Writing", change);
		var data = converter.take(path);
		localBlobId = insertBlob(data);
		if (idStore != null) {
			idStore.put(path, localBlobId);
		}
		progressMonitor.worked(1);
		return localBlobId;
	}

	private void appendPackageInfo(TreeFormatter tree) {
		try {
			var schemaBytes = PackageInfo.create()
					.withLibraries(database.getLibraries())
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

	private boolean matches(String path, Change change, File file) {
		if (change == null)
			return false;
		if (file == null)
			return path.equals(change.path);
		return path.startsWith(change.path.substring(0, change.path.lastIndexOf(GitUtil.DATASET_SUFFIX)));
	}

	private ObjectId commit(String message, ObjectId treeId) {
		try {
			var commit = new CommitBuilder();
			commit.setAuthor(committer);
			commit.setCommitter(committer);
			commit.setMessage(message);
			commit.setEncoding(StandardCharsets.UTF_8);
			commit.setTreeId(treeId);
			if (!Strings.nullOrEmpty(localCommitId)) {
				commit.addParentId(ObjectId.fromString(localCommitId));
			}
			if (!Strings.nullOrEmpty(remoteCommitId)) {
				commit.addParentId(ObjectId.fromString(remoteCommitId));
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

	private void close() throws IOException {
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
		if (converter != null) {
			converter.clear();
			converter = null;
		}
		if (threads != null) {
			threads.shutdown();
		}
	}

}
