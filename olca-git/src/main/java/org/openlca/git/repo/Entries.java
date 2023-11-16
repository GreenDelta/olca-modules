package org.openlca.git.repo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.openlca.git.model.Entry;
import org.openlca.git.model.Entry.EntryType;
import org.openlca.git.util.GitUtil;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Entries {

	private static final Logger log = LoggerFactory.getLogger(Entries.class);
	private final OlcaRepository repo;

	static Entries of(OlcaRepository repo) {
		return new Entries(repo);
	}

	private Entries(OlcaRepository repo) {
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
			var commit = repo.commits.getRev(commitId);
			if (commit == null)
				return null;
			var objectId = repo.getSubTreeId(commit.getTree().getId(), path);
			return new Entry(path, commitId, objectId);
		} catch (IOException e) {
			log.error("Error finding sub tree for " + path);
			return null;
		}
	}

	public class Find {

		private final Iterate iterate = new Iterate();
		
		public Find path(String path) {
			iterate.path(path);
			return this;
		}

		public Find commit(String commitId) {
			iterate.commit(commitId);
			return this;
		}

		public Find recursive() {
			iterate.recursive();
			return this;
		}

		public List<Entry> all() {
			var entries = new ArrayList<Entry>();
			iterate.call(entries::add);
			return entries;
		}
		
		public Map<String, Entry> asMap() {
			return all().stream().collect(Collectors.toMap(e -> e.path, e -> e));
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
				commit = repo.commits.getRev(commitId);
				if (commit == null)
					return;
				var treeId = Strings.nullOrEmpty(path)
						? commit.getTree().getId()
						: repo.getSubTreeId(commit.getTree().getId(), path);
				if (treeId.equals(ObjectId.zeroId()))
					return;
				try (var walk = new TreeWalk(repo)) {
					walk.addTree(treeId);
					walk.setRecursive(false);
					var filter = KnownFilesFilter.createForPath(path);
					walk.setFilter(filter);
					while (walk.next()) {
						var name = GitUtil.decode(walk.getNameString());
						var fullPath = Strings.nullOrEmpty(path) ? name : path + "/" + name;
						var entry = new Entry(fullPath, commit.getName(), walk.getObjectId(0));
						consumer.accept(entry);
						if (recursive && entry.typeOfEntry != EntryType.DATASET) {
							new Iterate().commit(commitId).recursive().path(fullPath).call(consumer);
						}
					}
				}
			} catch (IOException e) {
				log.error("Error walking commit " + (commit != null ? commit.getName() : commitId), e);
			}
		}

	}

}
