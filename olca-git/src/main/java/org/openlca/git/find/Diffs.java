package org.openlca.git.find;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.openlca.core.database.IDatabase;
import org.openlca.git.GitIndex;
import org.openlca.git.iterator.DatabaseIterator;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.model.DiffType;
import org.openlca.git.model.Reference;
import org.openlca.git.util.Descriptors;
import org.openlca.git.util.GitUtil;
import org.openlca.git.util.Repositories;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Diffs {

	private static Logger log = LoggerFactory.getLogger(Diffs.class);
	private final Repository repo;
	private final Commit commit;
	private RevCommit leftCommit;
	private RevCommit rightCommit;
	private GitIndex index;
	private Descriptors descriptors;
	private String path;
	private boolean onlyCategories;
	private boolean excludeCategories;

	private Diffs(Repository repo, Commit commit) {
		this.repo = repo;
		this.commit = commit;
	}

	public static Diffs of(Repository repo) {
		return new Diffs(repo, null);
	}

	public static Diffs of(Repository repo, Commit commit) {
		return new Diffs(repo, commit);
	}

	public Diffs filter(String path) {
		this.path = path;
		return this;
	}

	public Diffs excludeCategories() {
		this.excludeCategories = true;
		return this;
	}

	public Diffs onlyCategories() {
		this.onlyCategories = true;
		return this;
	}

	public List<Diff> with(IDatabase database, GitIndex gitIndex) {
		this.index = gitIndex;
		this.descriptors = Descriptors.of(database);
		this.leftCommit = getRevCommit(commit, true);
		return diffOfDatabase(path);
	}

	private List<Diff> diffOfDatabase(String prefix) {
		try (var walk = new TreeWalk(repo)) {
			var repoIterator = createIterator(leftCommit, prefix);
			var dbIterator = new DatabaseIterator(index, descriptors, prefix);
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
		var leftCommit = Commits.of(repo).find().before(commit.id).latest();
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
				: Repositories.getSubTreeId(repo, commit.getTree().getId(), path);
		if (ObjectId.zeroId().equals(treeId))
			return new EmptyTreeIterator();
		var it = new CanonicalTreeParser();
		it.reset(repo.newObjectReader(), treeId);
		return it;
	}

	private RevCommit getRevCommit(Commit commit, boolean useHeadAsDefault) {
		try {
			var commitId = commit != null
					? ObjectId.fromString(commit.id)
					: null;
			if (commitId != null)
				return repo.parseCommit(commitId);
			if (useHeadAsDefault)
				return Repositories.headCommitOf(repo);
			return null;
		} catch (IOException e) {
			log.error("Error loading commit", e);
			return null;
		}
	}

	private List<Diff> scan(TreeWalk walk, String prefix, Function<String, List<Diff>> scan) throws IOException {
		var diffs = new ArrayList<Diff>();
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
			} else if (oldMode == FileMode.REGULAR_FILE && newMode == FileMode.REGULAR_FILE && !oldId.equals(newId)) {
				addDiff(diffs, DiffType.MODIFIED, path, oldId, newId);
			}
			if (oldMode == FileMode.TREE || newMode == FileMode.TREE) {
				diffs.addAll(scan.apply(path));
			}
		}
		return diffs;
	}

	private void addDiff(List<Diff> diffs, DiffType type, String path, ObjectId oldId, ObjectId newId)
			throws IOException {
		path = GitUtil.decode(path);
		if (!path.contains("/"))
			return;
		var oldPath = isEmptyCategory(createIterator(leftCommit, path))
				? path + "/" + GitUtil.EMPTY_CATEGORY_FLAG
				: path;
		var oldCommitId = leftCommit != null
				? leftCommit.getId().getName()
				: null;
		var oldRef = oldId != null
				? new Reference(oldPath, oldCommitId, oldId)
				: null;
		var isEmptyCategory = descriptors != null
				? isEmptyCategory(new DatabaseIterator(index, descriptors, path))
				: isEmptyCategory(createIterator(rightCommit, path));
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
		diffs.add(diff);
	}

	private boolean isEmptyCategory(AbstractTreeIterator iterator) throws IOException {
		if (iterator.eof())
			return false;
		var subPath = GitUtil.decode(iterator.getEntryPathString());
		return subPath.equals(GitUtil.EMPTY_CATEGORY_FLAG);
	}

}
