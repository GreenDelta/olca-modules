package org.openlca.git.repo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.openlca.git.iterator.DatabaseIterator;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.model.DiffType;
import org.openlca.git.model.Reference;
import org.openlca.git.util.GitUtil;
import org.openlca.git.util.TypedRefIdMap;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Diffs {

	private static Logger log = LoggerFactory.getLogger(Diffs.class);
	private final OlcaRepository repo;

	private Diffs(OlcaRepository repo) {
		this.repo = repo;
	}

	static Diffs of(OlcaRepository repo) {
		return new Diffs(repo);
	}

	public Find find() {
		return new Find();
	}

	public class Find {

		private Commit commit;
		private RevCommit leftCommit;
		private RevCommit rightCommit;
		private String path;
		private boolean onlyCategories;
		private boolean excludeCategories;

		public Find commit(Commit commit) {
			this.commit = commit;
			return this;
		}

		public Find filter(String path) {
			this.path = path;
			return this;
		}

		public Find excludeCategories() {
			this.excludeCategories = true;
			return this;
		}

		public Find onlyCategories() {
			this.onlyCategories = true;
			return this;
		}

		public List<Diff> withDatabase() {
			if (!(repo instanceof ClientRepository))
				throw new UnsupportedOperationException("Can only execute diff with database on ClientRepository");
			this.leftCommit = getRevCommit(commit, true);
			return diffOfDatabase(path);
		}

		private List<Diff> diffOfDatabase(String prefix) {
			try (var walk = new TreeWalk(repo)) {
				var repoIterator = createIterator(leftCommit, prefix);
				var dbIterator = createDatabaseIterator(prefix);
				walk.addTree(repoIterator);
				walk.addTree(dbIterator);
				walk.setFilter(KnownFilesFilter.createForPath(prefix));
				walk.setRecursive(false);
				return scan(walk, prefix, path -> diffOfDatabase(path));
			} catch (IOException e) {
				log.error("Error getting diffs for path " + prefix, e);
				return new ArrayList<>();
			}
		}

		public List<Diff> withPreviousCommit() {
			var leftCommit = repo.commits.find().before(commit.id).latest();
			this.leftCommit = getRevCommit(leftCommit, false);
			this.rightCommit = getRevCommit(commit, false);
			return diffOfCommits(path);
		}

		public List<Diff> with(Commit other) {
			this.leftCommit = getRevCommit(commit, false);
			this.rightCommit = getRevCommit(other, false);
			return diffOfCommits(path);
		}

		private List<Diff> diffOfCommits(String prefix) {
			try (var walk = new TreeWalk(repo)) {
				walk.addTree(createIterator(leftCommit, prefix));
				walk.addTree(createIterator(rightCommit, prefix));
				walk.setFilter(KnownFilesFilter.createForPath(prefix));
				walk.setRecursive(false);
				return scan(walk, prefix, path -> diffOfCommits(path));
			} catch (IOException e) {
				log.error("Error adding tree", e);
				return new ArrayList<>();
			}
		}

		private AbstractTreeIterator createIterator(RevCommit commit, String path)
				throws IOException {
			if (commit == null)
				return new EmptyTreeIterator();
			var treeId = Strings.nullOrEmpty(path)
					? commit.getTree().getId()
					: repo.getSubTreeId(commit.getTree().getId(), path);
			if (ObjectId.zeroId().equals(treeId))
				return new EmptyTreeIterator();
			var it = new CanonicalTreeParser();
			it.reset(repo.newObjectReader(), treeId);
			return it;
		}

		private AbstractTreeIterator createDatabaseIterator(String path) {
			if (repo instanceof ClientRepository r && r.database != null)
				return new DatabaseIterator(r, path);
			return new EmptyTreeIterator();
		}

		private RevCommit getRevCommit(Commit commit, boolean useHeadAsDefault) {
			try {
				var commitId = commit != null
						? ObjectId.fromString(commit.id)
						: null;
				if (commitId != null)
					return repo.parseCommit(commitId);
				if (useHeadAsDefault)
					return repo.getHeadCommit();
				return null;
			} catch (IOException e) {
				log.error("Error loading commit", e);
				return null;
			}
		}

		private List<Diff> scan(TreeWalk walk, String prefix, Function<String, List<Diff>> scan) throws IOException {
			var diffs = new TypedRefIdMap<Diff>();
			while (walk.next()) {
				var path = !Strings.nullOrEmpty(prefix)
						? prefix + "/" + walk.getPathString()
						: walk.getPathString();
				var oldMode = walk.getFileMode(0);
				var newMode = walk.getFileMode(1);
				var oldId = walk.getObjectId(0);
				var newId = walk.getObjectId(1);
				if (oldMode == FileMode.MISSING) {
					addDiff(diffs, DiffType.ADDED, path, null, newId);
				} else if (newMode == FileMode.MISSING) {
					addDiff(diffs, DiffType.DELETED, path, oldId, null);
				} else if (oldMode == FileMode.REGULAR_FILE && newMode == FileMode.REGULAR_FILE
						&& !oldId.equals(newId)) {
					addDiff(diffs, DiffType.MODIFIED, path, oldId, newId);
				}
				if (oldMode == FileMode.TREE || newMode == FileMode.TREE) {
					scan.apply(path).forEach(diff -> merge(diffs, diff));
				}
			}
			return diffs.values();
		}

		private void addDiff(TypedRefIdMap<Diff> diffs, DiffType type, String path, ObjectId oldId, ObjectId newId)
				throws IOException {
			path = GitUtil.decode(path);
			if (!path.contains("/"))
				return;
			var oldPath = isEmptyCategory(leftCommit, path)
					? path + "/" + GitUtil.EMPTY_CATEGORY_FLAG
					: path;
			var oldCommitId = leftCommit != null
					? leftCommit.getId().getName()
					: null;
			var oldRef = oldId != null
					? new Reference(oldPath, oldCommitId, oldId)
					: null;
			var isEmptyCategory = isEmptyCategory(rightCommit, path);
			var newPath = isEmptyCategory
					? path + "/" + GitUtil.EMPTY_CATEGORY_FLAG
					: path;
			var newCommitId = rightCommit != null
					? rightCommit.getName()
					: null;
			var newRef = new Reference(newPath, newCommitId, newId);
			var diff = new Diff(type, oldRef, newRef);
			if (diff.isCategory && (type == DiffType.MODIFIED || excludeCategories))
				return;
			if (!diff.isCategory && onlyCategories)
				return;
			merge(diffs, diff);
		}

		private void merge(TypedRefIdMap<Diff> diffs, Diff diff) {
			var other = diffs.get(diff);
			if (other == null) {
				diffs.put(diff, diff);
				return;
			}
			if (diff.diffType == DiffType.DELETED && other.diffType == DiffType.ADDED) {
				if (diff.path.equals(other.path)) {
					diffs.remove(diff);
				} else {
					var left = new Reference(diff.path, diff.oldCommitId, diff.oldObjectId);
					var right = new Reference(other.path, other.newCommitId, other.newObjectId);
					diffs.put(diff, new Diff(DiffType.MOVED, left, right));
				}
			} else if (diff.diffType == DiffType.ADDED && other.diffType == DiffType.DELETED) {
				if (diff.path.equals(other.path)) {
					diffs.remove(diff);
				} else {
					var left = new Reference(other.path, other.oldCommitId, other.oldObjectId);
					var right = new Reference(diff.path, diff.newCommitId, diff.newObjectId);
					diffs.put(diff, new Diff(DiffType.MOVED, left, right));
				}
			}
		}

		private boolean isEmptyCategory(RevCommit commit, String path) throws IOException {
			if (path.endsWith(GitUtil.DATASET_SUFFIX))
				return false;
			var iterator = commit != null
					? createIterator(commit, path)
					: createDatabaseIterator(path);
			if (iterator.eof())
				return false;
			var subPath = GitUtil.decode(iterator.getEntryPathString());
			return subPath.equals(GitUtil.EMPTY_CATEGORY_FLAG);
		}

	}

}
