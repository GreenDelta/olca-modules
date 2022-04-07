package org.openlca.git.find;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.openlca.git.model.Entry;
import org.openlca.git.util.GitUtil;
import org.openlca.jsonld.SchemaVersion;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Entries {

	private static final Logger log = LoggerFactory.getLogger(Entries.class);
	private final FileRepository repo;

	public static Entries of(FileRepository repo) {
		return new Entries(repo);
	}

	private Entries(FileRepository repo) {
		this.repo = repo;
	}

	public Find find() {
		return new Find();
	}

	public class Find {

		private String path;
		private String commitId;

		public Find path(String path) {
			this.path = GitUtil.encode(path);
			return this;
		}

		public Find commit(String commitId) {
			this.commitId = commitId;
			return this;
		}

		public List<Entry> all() {
			var entries = new ArrayList<Entry>();
			try {
				var commits = Commits.of(repo);
				var ids = Ids.of(repo);
				var commit = commits.getRev(commitId);
				if (commit == null)
					return entries;
				var treeId = ids.get(commit.getTree().getId(), path);
				if (treeId.equals(ObjectId.zeroId()))
					return entries;
				try (var walk = new TreeWalk(repo)) {
					walk.addTree(treeId);
					walk.setRecursive(false);
					var filter = AndTreeFilter.create(
							NotBinaryFilter.create(),
							PathFilter.create(SchemaVersion.FILE_NAME).negate());
					walk.setFilter(filter);
					while (walk.next()) {
						var name = walk.getNameString();
						var fullPath = Strings.nullOrEmpty(path) ? name : path + "/" + name;
						entries.add(new Entry(fullPath, commitId, walk.getObjectId(0)));
					}
				}
			} catch (IOException e) {
				log.error("Error walking commit + " + commitId);
			}
			return entries;
		}

	}

}
