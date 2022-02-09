package org.openlca.git.writer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Executors;

import org.eclipse.jgit.internal.storage.file.PackInserter;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.TreeFormatter;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.openlca.git.Config;
import org.openlca.git.iterator.DiffIterator;
import org.openlca.git.model.Diff;
import org.openlca.git.model.DiffType;
import org.openlca.git.util.GitUtil;
import org.openlca.jsonld.SchemaVersion;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

// TODO check error handling
public class CommitWriter {

	private static final Logger log = LoggerFactory.getLogger(CommitWriter.class);
	private final Config config;
	private PackInserter inserter;
	private Converter converter;

	public CommitWriter(Config config) {
		this.config = config;
	}

	public String commit(String message, List<Diff> diffs) throws IOException {
		if (diffs.isEmpty())
			return null;
		var threads = Executors.newCachedThreadPool();
		try {
			var previousCommitTreeId = getPreviousCommitTreeId();
			var isFirstCommit = previousCommitTreeId == null || previousCommitTreeId.equals(ObjectId.zeroId());
			if (!isFirstCommit && !isCurrentSchemaVersion(previousCommitTreeId))
				throw new IOException("Git repo is not in current schema version");
			inserter = config.repo.getObjectDatabase().newPackInserter();
			inserter.checkExisting(config.checkExisting);
			converter = new Converter(config, threads);
			converter.start(diffs.stream()
					.filter(d -> d.type != DiffType.DELETED)
					.toList());
			var treeId = syncTree("", previousCommitTreeId, new DiffIterator(config, diffs));
			config.store.save();
			var commitId = commit(treeId, message);
			return commitId.name();
		} finally {
			if (inserter != null) {
				inserter.flush();
				inserter.close();
			}
			if (converter != null) {
				converter.clear();
			}
			threads.shutdown();
		}
	}

	private ObjectId syncTree(String prefix, ObjectId treeId, DiffIterator diffIterator) {
		boolean appended = false;
		var tree = new TreeFormatter();
		try (var walk = createWalk(prefix, treeId, diffIterator)) {
			while (walk.next()) {
				var mode = walk.getFileMode();
				var name = walk.getNameString();
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
			log.error("Error walking tree " + treeId, e);
		}
		if (!appended && !Strings.nullOrEmpty(prefix)) {
			config.store.invalidate(prefix);
			return null;
		}
		if (Strings.nullOrEmpty(prefix) && (treeId == null || treeId.equals(ObjectId.zeroId()))) {
			appendSchemaVersion(tree);
		}
		var newId = insert(i -> i.insert(tree));
		config.store.put(prefix, newId);
		return newId;
	}

	private TreeWalk createWalk(String prefix, ObjectId treeId, DiffIterator diffIterator) throws IOException {
		var walk = new TreeWalk(config.repo);
		if (treeId == null || treeId.equals(ObjectId.zeroId())) {
			walk.addTree(new EmptyTreeIterator());
		} else if (Strings.nullOrEmpty(prefix)) {
			walk.addTree(treeId);
		} else {
			walk.addTree(new CanonicalTreeParser(prefix.getBytes(), walk.getObjectReader(), treeId));
		}
		walk.addTree(diffIterator);
		return walk;
	}

	private ObjectId handleTree(TreeWalk walk, DiffIterator diffIterator) {
		var treeId = walk.getObjectId(0);
		if (walk.getFileMode(1) == FileMode.MISSING)
			return treeId;
		var prefix = walk.getPathString();
		return syncTree(prefix, treeId, diffIterator.createSubtreeIterator());
	}

	private ObjectId handleFile(TreeWalk walk)
			throws IOException, InterruptedException {
		var blobId = walk.getObjectId(0);
		if (walk.getFileMode(1) == FileMode.MISSING)
			return blobId;
		var path = walk.getPathString();
		var iterator = walk.getTree(1, DiffIterator.class);
		Diff diff = iterator.getEntryData();
		var file = iterator.getEntryFile();
		if (diff.type == DiffType.DELETED && matches(path, diff, file)) {
			if (file == null) {
				config.store.invalidate(path);
			}
			return null;
		}
		if (file != null)
			return inserter.insert(Constants.OBJ_BLOB, Files.readAllBytes(file.toPath()));
		var data = converter.take(path);
		blobId = inserter.insert(Constants.OBJ_BLOB, data);
		config.store.put(path, blobId);
		return blobId;
	}

	private void appendSchemaVersion(TreeFormatter tree) {
		try {
			var schemaBytes = SchemaVersion.current().toJson().toString().getBytes(StandardCharsets.UTF_8);
			var blobId = inserter.insert(Constants.OBJ_BLOB, schemaBytes);
			if (blobId != null) {
				tree.append(SchemaVersion.FILE_NAME, FileMode.REGULAR_FILE, blobId);
			}
		} catch (Exception e) {
			log.error("Error inserting schema version", e);
		}
	}

	private boolean matches(String path, Diff diff, File file) {
		if (diff == null)
			return false;
		if (file == null)
			return path.equals(diff.path());
		return path.startsWith(diff.path().substring(0, diff.path().lastIndexOf(GitUtil.DATASET_SUFFIX)));
	}

	private ObjectId getPreviousCommitTreeId() {
		try (var walk = new RevWalk(config.repo)) {
			var head = config.repo.resolve("refs/heads/master");
			if (head == null)
				return null;
			var commit = walk.parseCommit(head);
			if (commit == null)
				return null;
			return commit.getTree().getId();
		} catch (IOException e) {
			log.error("Error reading commit tree", e);
			return null;
		}
	}

	private ObjectId commit(ObjectId treeId, String message) {
		try {
			var commit = new CommitBuilder();
			commit.setAuthor(config.committer);
			commit.setCommitter(config.committer);
			commit.setMessage(message);
			commit.setEncoding(StandardCharsets.UTF_8);
			commit.setTreeId(treeId);
			var head = config.repo.findRef("HEAD");
			var previousCommitId = head.getObjectId();
			if (previousCommitId != null) {
				commit.addParentId(previousCommitId);
			}
			var commitId = insert(i -> i.insert(commit));
			var update = config.repo.updateRef(head.getName());
			update.setNewObjectId(commitId);
			update.update();
			return commitId;
		} catch (IOException e) {
			log.error("failed to update head", e);
			return null;
		}
	}

	private ObjectId insert(Insert insertion) {
		try (var inserter = config.repo.newObjectInserter()) {
			return insertion.insertInto(inserter);
		} catch (IOException e) {
			log.error("failed to insert", e);
			return null;
		}
	}

	private boolean isCurrentSchemaVersion(ObjectId treeId) {
		try (var walk = new TreeWalk(config.repo);
				var reader = config.repo.getObjectDatabase().newReader()) {
			walk.addTree(treeId);
			walk.setRecursive(false);
			walk.setFilter(PathFilter.create(SchemaVersion.FILE_NAME));
			if (!walk.next())
				return false;
			var blobId = walk.getObjectId(0);
			var bytes = reader.open(blobId).getBytes();
			var json = new Gson().fromJson(new String(bytes, StandardCharsets.UTF_8), JsonElement.class);
			var schema = SchemaVersion.of(json);
			return schema.isCurrent();
		} catch (IOException e) {
			log.error("failed to check for schema version", e);
			return false;
		}
	}

	private interface Insert {

		ObjectId insertInto(ObjectInserter inserter) throws IOException;

	}

}