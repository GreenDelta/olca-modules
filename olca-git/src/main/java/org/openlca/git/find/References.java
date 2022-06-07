package org.openlca.git.find;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.openlca.core.model.ModelType;
import org.openlca.git.model.Reference;
import org.openlca.git.util.GitUtil;
import org.openlca.jsonld.PackageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class References {

	static final Logger log = LoggerFactory.getLogger(References.class);
	private final Repository repo;

	public static References of(Repository repo) {
		return new References(repo);
	}

	private References(Repository repo) {
		this.repo = repo;
	}

	public Reference get(ModelType type, String refId, String commitId) {
		var refs = find().model(type, refId).commit(commitId).all();
		if (refs.isEmpty())
			return null;
		return refs.get(0);
	}

	public List<String> getBinaries(Reference ref) {
		if (ref == null)
			return new ArrayList<>();
		try {
			var commit = Commits.of(repo).getRev(ref.commitId);
			if (commit == null)
				return new ArrayList<>();
			try (var walk = new TreeWalk(repo)) {
				var paths = new ArrayList<String>();
				walk.addTree(commit.getTree());
				walk.setFilter(PathFilter.create(ref.getBinariesPath()));
				walk.setRecursive(true);
				while (walk.next()) {
					paths.add(walk.getNameString());
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

	private Reference createRef(TreeWalk walk, String commitId, int tree) {
		return new Reference(walk.getPathString(), commitId, walk.getObjectId(tree));
	}

	public class Find {

		private String path;
		private String commitId;
		private ModelType type;
		private String refId;

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
			return this;
		}

		public long count() {
			return get(true).size();
		}

		public List<Reference> all() {
			return get(false);
		}

		private List<Reference> get(boolean countOnly) {
			try {
				var commits = Commits.of(repo);
				var commit = commits.getRev(commitId);
				if (commit == null)
					return new ArrayList<>();
				var commitId = commit.getId().name();
				try (var walk = new TreeWalk(repo)) {
					var refs = new ArrayList<Reference>();
					walk.addTree(commit.getTree());
					walk.setRecursive(true);
					var filter = AndTreeFilter.create(
							NotBinaryFilter.create(),
							PathFilter.create(PackageInfo.FILE_NAME).negate());
					if (path != null) {
						filter = AndTreeFilter.create(filter, PathFilter.create(GitUtil.encode(path)));
					}
					if (type != null && refId != null) {
						filter = AndTreeFilter.create(filter, new ModelFilter(type, refId));
					}
					walk.setFilter(filter);
					while (walk.next()) {
						if (countOnly) {
							refs.add(null);
						} else {
							refs.add(createRef(walk, commitId, 0));
						}
					}
					return refs;
				}
			} catch (IOException e) {
				log.error("Error getting references, type: " + type + ", refId: " + refId + ", commit: " + commitId
						+ ", path: " + path, e);
				return new ArrayList<>();
			}
		}

	}

}
