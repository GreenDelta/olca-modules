package org.openlca.git.repo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.openlca.core.model.ModelType;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.model.Reference;
import org.openlca.git.util.GitUtil;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class References {

	static final Logger log = LoggerFactory.getLogger(References.class);
	private final OlcaRepository repo;

	static References of(OlcaRepository repo) {
		return new References(repo);
	}

	private References(OlcaRepository repo) {
		this.repo = repo;
	}

	public Reference get(String path, String commitId) {
		try {
			var commit = repo.commits.getRev(commitId);
			if (commit == null)
				return null;
			if (path.startsWith(RepositoryInfo.FILE_NAME + "/"))
				return new Reference(path, commitId, null);
			var objectId = repo.getObjectId(commit, path);
			return new Reference(path, commitId, objectId);
		} catch (IOException e) {
			log.error("Error finding sub tree for " + path);
			return null;
		}
	}

	public Reference get(ModelType type, String refId, String commitId) {
		return find().model(type, refId).commit(commitId).first();
	}

	public List<String> getBinaries(Reference ref) {
		if (ref == null)
			return new ArrayList<>();
		try {
			var commit = repo.commits.getRev(ref.commitId);
			if (commit == null)
				return new ArrayList<>();
			try (var walk = new TreeWalk(repo)) {
				var paths = new ArrayList<String>();
				walk.addTree(commit.getTree());
				walk.setFilter(PathFilter.create(GitUtil.encode(ref.getBinariesPath())));
				walk.setRecursive(true);
				while (walk.next()) {
					paths.add(GitUtil.decode(walk.getNameString()));
				}
				return paths;
			}
		} catch (IOException e) {
			log.error("Error getting binaries", e);
			return null;
		}
	}

	public Find find() {
		return new Find();
	}

	public class Find {

		private String path;
		private String commitId;
		private ModelType type;
		private String refId;
		private boolean includeCategories;
		private boolean recursive = true;

		public Find path(String path) {
			this.path = path;
			return this;
		}

		public Find commit(String commitId) {
			this.commitId = commitId;
			return this;
		}

		public Find type(ModelType type) {
			this.path = type != null ? type.name() : null;
			return this;
		}

		public Find model(ModelType type, String refId) {
			this.type = type;
			this.refId = refId;
			this.includeCategories = false;
			return this;
		}

		public Find includeCategories() {
			this.includeCategories = true;
			return this;
		}

		public Find nonRecursive() {
			this.recursive = false;
			return this;
		}

		public long count() {
			var count = new AtomicLong();
			iterate(ref -> {
				count.addAndGet(1);
				return true;
			});
			return count.get();
		}

		public Reference first() {
			var refHolder = new ArrayList<Reference>();
			iterate(ref -> {
				refHolder.add(ref);
				return false;
			});
			if (refHolder.isEmpty())
				return null;
			return refHolder.get(0);
		}

		public boolean contains(String path) {
			var value = new AtomicBoolean(false);
			iterate(ref -> {
				if (ref.path.endsWith("/" + path)) {
					value.set(true);
					return false;
				}
				return true;
			});
			return value.get();
		}

		public Map<String, Reference> asMap() {
			var map = new HashMap<String, Reference>();
			iterate(ref -> {
				// TODO how to avoid duplicate keys or handle them better
				if (map.containsKey(ref.path))
					return true;
				map.put(ref.path, ref);
				return true;
			});
			return map;
		}
		
		public void iterate(Consumer<Reference> consumer) {
			iterate(ref -> {
				consumer.accept(ref);
				return true;
			});
		}

		private void iterate(Function<Reference, Boolean> consumer) {
			try {
				var commit = repo.commits.getRev(commitId);
				if (commit == null)
					return;
				if (!includeCategories && recursive) {
					iterateModels(commit, consumer);
				} else {
					var treeId = Strings.nullOrEmpty(path)
							? commit.getTree().getId()
							: repo.getSubTreeId(commit.getTree().getId(), path);
					if (treeId.equals(ObjectId.zeroId()))
						return;
					iterate(treeId, commit, path, consumer);
				}
			} catch (IOException e) {
				log.error("Error walking commit " + commitId, e);
			}
		}

		private void iterateModels(RevCommit commit, Function<Reference, Boolean> consumer) {
			try {
				try (var walk = new TreeWalk(repo)) {
					walk.addTree(commit.getTree());
					walk.setRecursive(true);
					TreeFilter filter = KnownFilesFilter.create();
					if (path != null) {
						filter = AndTreeFilter.create(filter, PathFilter.create(GitUtil.encode(path)));
					}
					if (type != null && refId != null) {
						filter = AndTreeFilter.create(filter, new ModelFilter(type, refId));
					}
					walk.setFilter(filter);
					while (walk.next()) {
						var id = walk.getObjectId(0);
						var path = GitUtil.decode(walk.getPathString());
						var ref = new Reference(path, commit.getName(), id);
						if (!consumer.apply(ref))
							break;
					}
				}
			} catch (IOException e) {
				log.error("Error getting references", e);
			}
		}

		private void iterate(ObjectId treeId, RevCommit commit, String path, Function<Reference, Boolean> consumer) {
			try {
				if (RepositoryInfo.FILE_NAME.equals(path)) {
					var info = repo.getInfo(commit);
					var libs = info.libraries().stream()
							.map(library -> new Reference(RepositoryInfo.FILE_NAME + "/" + library.id(),
									commit.getName(), null))
							.collect(Collectors.toList());
					for (var lib : libs)
						if (!consumer.apply(lib))
							return;
				}
				try (var walk = new TreeWalk(repo)) {
					walk.addTree(treeId);
					walk.setRecursive(false);
					var filter = KnownFilesFilter.createForPath(path).includeLibraries();
					walk.setFilter(filter);
					while (walk.next()) {
						var name = GitUtil.decode(walk.getNameString());
						if (name.equals(RepositoryInfo.FILE_NAME) && repo.getInfo(commit).libraries().isEmpty())
							continue;
						var id = walk.getObjectId(0);
						var fullPath = name;
						if (!Strings.nullOrEmpty(path)) {
							fullPath = path + "/" + name;
						}
						var ref = new Reference(fullPath, commit.getName(), id);
						if (!consumer.apply(ref))
							break;
						if (recursive && !ref.isDataset) {
							iterate(id, commit, fullPath, consumer);
						}
					}
				}
			} catch (IOException e) {
				log.error("Error getting references", e);
			}
		}

	}

}
