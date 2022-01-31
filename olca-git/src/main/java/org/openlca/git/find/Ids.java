package org.openlca.git.find;

import java.io.IOException;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ids {

	static final Logger log = LoggerFactory.getLogger(References.class);
	private final FileRepository repo;

	public static Ids of(FileRepository repo) {
		return new Ids(repo);
	}
	
	private Ids(FileRepository repo) {
		this.repo = repo;
	}

	public ObjectId get(String path) {
		return get(path, null);
	}

	public ObjectId get(String path, String commitId) {
		try {
			var commits = Commits.of(repo);
			var commit = commits.getRev(commitId);
			if (commit == null)
				return ObjectId.zeroId();
			return get(commit.getTree().getId(), path);
		} catch (IOException e) {
			log.error("Error finding sub tree for " + path);
			return null;
		}
	}

	ObjectId get(ObjectId treeId, String path) {
		if (Strings.nullOrEmpty(path))
			return treeId;
		var name = path.contains("/") ? path.substring(0, path.indexOf("/")) : path;
		var rest = path.contains("/") ? path.substring(path.indexOf("/") + 1) : null;
		try (var walk = new TreeWalk(repo)) {
			walk.addTree(treeId);
			walk.setRecursive(false);
			while (walk.next()) {
				if (rest != null && walk.getFileMode() != FileMode.TREE)
					continue;
				if (!name.equals(walk.getNameString()))
					continue;
				var subTreeId = walk.getObjectId(0);
				if (rest == null)
					return subTreeId;
				return get(subTreeId, rest);
			}
			return ObjectId.zeroId();
		} catch (IOException e) {
			log.error("Error finding sub tree for " + path);
			return ObjectId.zeroId();
		}
	}

}
