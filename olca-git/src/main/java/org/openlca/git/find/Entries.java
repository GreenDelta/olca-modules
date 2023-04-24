package org.openlca.git.find;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.openlca.git.model.Entry;
import org.openlca.git.model.Entry.EntryType;
import org.openlca.git.util.GitUtil;
import org.openlca.jsonld.PackageInfo;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Entries {

	private static final Logger log = LoggerFactory.getLogger(Entries.class);
	private final Repository repo;

	public static Entries of(Repository repo) {
		return new Entries(repo);
	}

	private Entries(Repository repo) {
		this.repo = repo;
	}

	public Find find() {
		return new Find();
	}

	public void iterate(String commitId, Consumer<Entry> consumer) {
		new Iterate().commit(commitId).recursive().call(consumer);
	}

	public Entry get(String path, String commitId) {
		try {
			var commits = Commits.of(repo);
			var commit = commits.getRev(commitId);
			if (commit == null)
				return null;
			var objectId = get(commit.getTree().getId(), path);
			return new Entry(path, commitId, objectId);
		} catch (IOException e) {
			log.error("Error finding sub tree for " + path);
			return null;
		}
	}

	private ObjectId get(ObjectId treeId, String path) {
		if (Strings.nullOrEmpty(path))
			return treeId;
		try (var walk = TreeWalk.forPath(repo, GitUtil.encode(path), treeId)) {
			if (walk == null)
				return ObjectId.zeroId();
			return walk.getObjectId(0);
		} catch (IOException e) {
			log.error("Error finding entry for " + path);
			return null;
		}
	}

	public class Find {

		private String path;
		private String commitId;

		public Find path(String path) {
			this.path = path;
			return this;
		}

		public Find commit(String commitId) {
			this.commitId = commitId;
			return this;
		}

		public List<Entry> all() {
			var entries = new ArrayList<Entry>();
			new Iterate().commit(commitId).path(path).call(entries::add);
			return entries;
		}

	}

	private class Iterate {

		private String path;
		private String commitId;
		private boolean recursive;

		private Iterate path(String path) {
			this.path = path;
			return this;
		}

		private Iterate commit(String commitId) {
			this.commitId = commitId;
			return this;
		}

		private Iterate recursive() {
			this.recursive = true;
			return this;
		}

		private void call(Consumer<Entry> consumer) {
			RevCommit commit = null;
			try {
				var commits = Commits.of(repo);
				commit = commits.getRev(commitId);
				if (commit == null)
					return;
				var treeId = Strings.nullOrEmpty(path)
						? commit.getTree().getId()
						: get(commit.getTree().getId(), path);
				if (treeId.equals(ObjectId.zeroId()))
					return;
				try (var walk = new TreeWalk(repo)) {
					walk.addTree(treeId);
					walk.setRecursive(false);
					var filter = AndTreeFilter.create(
							NotBinaryFilter.create(),
							PathFilter.create(PackageInfo.FILE_NAME).negate());
					walk.setFilter(filter);
					while (walk.next()) {
						var name = GitUtil.decode(walk.getNameString());
						var fullPath = Strings.nullOrEmpty(path) ? name : path + "/" + name;
						var entry = new Entry(fullPath, commit.getName(), walk.getObjectId(0));
						consumer.accept(entry);
						if (recursive && entry.typeOfEntry != EntryType.DATASET) {
							new Iterate().commit(commitId).path(fullPath).call(consumer);
						}
					}
				}
			} catch (IOException e) {
				log.error("Error walking commit " + commit != null ? commit.getName() : commitId);
			}
		}

	}

}
