package org.openlca.git.repo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

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

	public void iterate(Consumer<Entry> consumer) {
		var head = repo.commits.head();
		if (head == null)
			return;
		iterate(head.id, consumer);
	}

	public void iterate(String commitId, Consumer<Entry> consumer) {
		iterate(commitId, null, consumer);
	}

	public void iterate(String commitId, String path, Consumer<Entry> consumer) {
		new Iterate().commit(commitId).path(path).recursive().call(consumer);
	}

	public Entry get(String path, String commitId) {
		try {
			var commit = repo.commits.getRev(commitId);
			if (commit == null)
				return null;
			var objectId = repo.getObjectId(commit, path);
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

		public long count() {
			var count = new AtomicLong(0);
			iterate.call(e -> count.incrementAndGet());
			return count.get();
		}

		public boolean contains(String path) {
			var value = new AtomicBoolean(false);
			iterate.call(e -> {
				if (e.path.endsWith("/" + path)) {
					value.set(true);
				}
			});
			return value.get();
		}

		public Map<String, Entry> asMap() {
			var map = new HashMap<String, Entry>();
			iterate.call(entry -> {
				// TODO how to avoid duplicate keys or handle them better
				if (map.containsKey(entry.path))
					return;
				map.put(entry.path, entry);
			});
			return map;
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
			try {
				var commit = repo.commits.getRev(commitId);
				if (commit == null)
					return;
				var treeId = Strings.nullOrEmpty(path)
						? commit.getTree().getId()
						: repo.getSubTreeId(commit.getTree().getId(), path);
				if (treeId.equals(ObjectId.zeroId()))
					return;
				call(treeId, commit, path, consumer);
			} catch (IOException e) {
				log.error("Error walking commit " + commitId, e);
			}
		}

		private void call(ObjectId treeId, RevCommit commit, String path, Consumer<Entry> consumer) throws IOException {
			try (var walk = new TreeWalk(repo)) {
				walk.addTree(treeId);
				walk.setRecursive(false);
				var filter = KnownFilesFilter.createForPath(path);
				walk.setFilter(filter);
				while (walk.next()) {
					var name = GitUtil.decode(walk.getNameString());
					var id = walk.getObjectId(0);
					var fullPath = Strings.nullOrEmpty(path) ? name : path + "/" + name;
					var entry = new Entry(fullPath, commit.getName(), id);
					consumer.accept(entry);
					if (recursive && entry.typeOfEntry != EntryType.DATASET) {
						call(id, commit, fullPath, consumer);
					}
				}
			}
		}

	}

}
