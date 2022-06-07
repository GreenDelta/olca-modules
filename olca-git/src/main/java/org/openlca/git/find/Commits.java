package org.openlca.git.find;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.openlca.core.model.ModelType;
import org.openlca.git.model.Commit;
import org.openlca.git.util.Constants;
import org.openlca.git.util.GitUtil;
import org.openlca.git.util.Repositories;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Commits {

	private static final Logger log = LoggerFactory.getLogger(Commits.class);
	private final Repository repo;

	public static Commits of(Repository repo) {
		return new Commits(repo);
	}

	private Commits(Repository repo) {
		this.repo = repo;
	}

	public Commit get(String id) {
		try {
			var rev = getRev(id);
			if (rev == null)
				return null;
			return new Commit(rev);
		} catch (IOException e) {
			log.error("Error accessing history", e);
			return null;
		}
	}

	public String resolve(String rev) {
		try {
			var id = repo.resolve(rev);
			if (id == null)
				return null;
			return id.getName();
		} catch (IOException e) {
			log.error("Error accessing history", e);
			return null;
		}
	}

	public Commit head() {
		var commit = Repositories.headCommitOf(repo);
		if (commit == null)
			return null;
		return new Commit(commit);
	}

	RevCommit getRev(String commitId) throws IOException {
		if (commitId != null)
			return repo.parseCommit(ObjectId.fromString(commitId));
		return Repositories.headCommitOf(repo);
	}

	public Find find() {
		return new Find();
	}

	public class Find {

		private String start;
		private boolean includeStart;
		private String end;
		private boolean includeEnd;
		private ModelType type;
		private String refId;
		private String path;
		private List<String> branches = List.of(Constants.LOCAL_BRANCH, Constants.LOCAL_REF);

		public Find from(String from) {
			this.includeStart = true;
			this.start = from;
			return this;
		}

		public Find after(String after) {
			this.includeStart = false;
			this.start = after;
			return this;
		}

		public Find until(String until) {
			this.includeEnd = true;
			this.end = until;
			return this;
		}

		public Find before(String before) {
			this.includeEnd = false;
			this.end = before;
			return this;
		}

		public Find path(String path) {
			this.path = path;
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

		public Find refs(String... branches) {
			this.branches = Arrays.asList(branches);
			return this;
		}

		public String latestId() {
			var all = all(true);
			if (all == null || all.isEmpty())
				return null;
			return all.get(all.size() - 1).id;
		}

		public Commit latest() {
			var all = all(true);
			if (all == null || all.isEmpty())
				return null;
			return all.get(all.size() - 1);
		}

		public List<Commit> all() {
			return all(false);
		}

		private List<Commit> all(boolean singleResult) {
			var commits = new LinkedHashSet<Commit>();
			try (var walk = walk()) {
				for (var commit : walk) {
					var commitId = commit.getId().name();
					if (!includeEnd && end != null && commitId.equals(end))
						continue;
					commits.add(new Commit(commit));
					if (singleResult)
						return new ArrayList<>(commits);
				}
				if (includeStart && start != null) {
					var commit = repo.parseCommit(toObjectId(start));
					commits.add(new Commit(commit));
				}
			} catch (IOException | GitAPIException e) {
				log.error("Error accessing history", e);
			}
			var list = new ArrayList<>(commits);
			Collections.reverse(list);
			return list;
		}

		private RevWalk walk() throws IOException, GitAPIException {
			var startId = toObjectId(start);
			var endId = toObjectId(end);
			var walk = new RevWalk(repo);
			if (endId == null && !repo.getRefDatabase().hasRefs())
				return walk;
			if (endId != null) {
				walk.markStart(walk.lookupCommit(endId));
			} else {
				for (var ref : repo.getRefDatabase().getRefs()) {
					if (branches.isEmpty() || branches.contains(ref.getName())) {
						var id = ref.getObjectId();
						if (startId != null && startId.equals(id))
							continue;
						walk.markStart(walk.parseCommit(id));
					}
				}
			}
			if (startId != null) {
				walk.markUninteresting(walk.parseCommit(startId));
			}
			TreeFilter filter = null;
			if (!Strings.nullOrEmpty(path)) {
				filter = addFilter(filter, PathFilter.create(GitUtil.encode(path)));
			}
			if (type != null && !Strings.nullOrEmpty(refId)) {
				filter = addFilter(filter, new ModelFilter(type, refId));
			}
			if (filter != null) {
				filter = addFilter(filter, TreeFilter.ANY_DIFF);
				walk.setTreeFilter(filter);
			}
			return walk;
		}

		private TreeFilter addFilter(TreeFilter current, TreeFilter newFilter) {
			return current != null ? AndTreeFilter.create(current, newFilter) : newFilter;
		}

		private ObjectId toObjectId(String value) {
			if (value == null)
				return null;
			return ObjectId.fromString(value);
		}

	}

}
