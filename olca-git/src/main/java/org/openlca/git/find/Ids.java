package org.openlca.git.find;

import java.io.IOException;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.openlca.git.util.GitUtil;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ids {

	static final Logger log = LoggerFactory.getLogger(References.class);
	private final Repository repo;

	public static Ids of(Repository repo) {
		return new Ids(repo);
	}

	private Ids(Repository repo) {
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
		try (var walk = TreeWalk.forPath(repo, GitUtil.encode(path), treeId)) {
			if (walk == null)
				return ObjectId.zeroId();
			return walk.getObjectId(0);
		} catch (IOException e) {
			log.error("Error finding id for " + path);
			return null;
		}
	}

}
