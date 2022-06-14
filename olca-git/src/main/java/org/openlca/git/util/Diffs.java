package org.openlca.git.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffEntry.Side;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.openlca.git.GitConfig;
import org.openlca.git.find.Commits;
import org.openlca.git.find.NotBinaryFilter;
import org.openlca.git.iterator.DatabaseIterator;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.model.DiffType;
import org.openlca.git.model.Reference;
import org.openlca.jsonld.PackageInfo;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Diffs {

	private static Logger log = LoggerFactory.getLogger(Diffs.class);

	public static List<Diff> workspace(GitConfig config) {
		return workspace(config, null, null);
	}

	public static List<Diff> workspace(GitConfig config, Commit commit) {
		return workspace(config, commit, null);
	}

	public static List<Diff> workspace(GitConfig config, Commit commit, List<String> paths) {
		try (var walk = new TreeWalk(config.repo)) {
			addTree(config.repo, walk, commit, true);
			walk.addTree(new DatabaseIterator(config));
			if (paths == null) {
				paths = new ArrayList<>();
			}
			walk.setFilter(getPathsFilter(paths.stream().distinct().toList()));
			walk.setRecursive(true);
			return scan(walk, e -> map(e, commit, null));
		} catch (IOException e) {
			log.error("Error adding tree", e);
			return new ArrayList<>();
		}
	}

	public static List<Diff> withPrevious(Repository repo, Commit commit) {
		return withPrevious(repo, commit, null);
	}

	public static List<Diff> withPrevious(Repository repo, Commit commit, List<String> paths) {
		var previousCommit = Commits.of(repo).find().before(commit.id).latest();
		return between(repo, previousCommit, commit);
	}

	public static List<Diff> between(Repository repo, Commit left, Commit right) {
		return between(repo, left, right, null);
	}

	public static List<Diff> between(Repository repo, Commit left, Commit right, List<String> paths) {
		try (var walk = new TreeWalk(repo)) {
			addTree(repo, walk, left, false);
			addTree(repo, walk, right, false);
			if (paths == null) {
				paths = new ArrayList<>();
			}
			walk.setFilter(getPathsFilter(paths.stream().distinct().toList()));
			walk.setRecursive(true);
			return scan(walk, e -> map(e, left, right));
		} catch (IOException e) {
			log.error("Error adding tree", e);
			return new ArrayList<>();
		}
	}

	private static List<Diff> scan(TreeWalk walk, Function<DiffEntry, Diff> map) {
		try {
			return DiffEntry.scan(walk).stream().map(map).collect(Collectors.toList());
		} catch (IOException e) {
			log.error("Error scanning walk", e);
			return new ArrayList<>();
		}
	}

	private static Diff map(DiffEntry e, Commit leftCommit, Commit rightCommit) {
		var type = getDiffType(e.getChangeType());
		var left = toCommitReference(leftCommit, e, Side.OLD);
		var right = toCommitReference(rightCommit, e, Side.NEW);
		return new Diff(type, left, right);
	}

	public static DiffType getDiffType(ChangeType type) {
		return switch (type) {
			case ADD -> DiffType.ADDED;
			case MODIFY -> DiffType.MODIFIED;
			case DELETE -> DiffType.DELETED;
			default -> throw new IllegalArgumentException("Unsupported change type: " + type);
		};
	}

	private static Reference toCommitReference(Commit commit, DiffEntry entry, Side side) {
		if (entry.getMode(side) == FileMode.MISSING)
			return null;
		var path = GitUtil.decode(entry.getPath(side));
		var objectId = entry.getId(side).toObjectId();
		var commitId = commit != null ? commit.id : null;
		return new Reference(path, commitId, objectId);
	}

	private static void addTree(Repository repo, TreeWalk walk, Commit commit, boolean useHeadAsDefault)
			throws IOException {
		var commitOid = commit != null
				? ObjectId.fromString(commit.id)
				: null;
		var revCommit = commitOid != null
				? repo.parseCommit(commitOid)
				: useHeadAsDefault
						? Repositories.headCommitOf(repo)
						: null;
		if (revCommit == null) {
			walk.addTree(new EmptyTreeIterator());
		} else {
			walk.addTree(revCommit.getTree().getId());
		}
	}

	private static TreeFilter getPathsFilter(List<String> paths) {
		var filter = PathFilter.create(PackageInfo.FILE_NAME).negate();
		filter = AndTreeFilter.create(filter, NotBinaryFilter.create());
		if (paths.isEmpty())
			return filter;
		for (var path : paths) {
			if (Strings.nullOrEmpty(path))
				continue;
			filter = AndTreeFilter.create(filter, PathFilter.create(GitUtil.encode(path)));
		}
		return filter;
	}

}
