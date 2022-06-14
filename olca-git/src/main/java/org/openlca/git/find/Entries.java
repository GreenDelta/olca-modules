package org.openlca.git.find;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.openlca.git.model.Entry;
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
			RevCommit commit = null;
			try {
				var commits = Commits.of(repo);
				var ids = Ids.of(repo);
				commit = commits.getRev(commitId);
				if (commit == null)
					return entries;
				var treeId = Strings.nullOrEmpty(path)
						? commit.getTree().getId()
						: ids.get(commit.getTree().getId(), path);
				if (treeId.equals(ObjectId.zeroId()))
					return entries;
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
						entries.add(new Entry(fullPath, commit.getName(), walk.getObjectId(0)));
					}
				}
			} catch (IOException e) {
				log.error("Error walking commit " + commit != null ? commit.getName() : commitId);
			}
			return entries;
		}

	}

}
