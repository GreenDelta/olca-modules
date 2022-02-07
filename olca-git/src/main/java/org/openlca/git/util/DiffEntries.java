package org.openlca.git.util;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.openlca.git.Config;
import org.openlca.git.iterator.DatabaseIterator;
import org.openlca.git.model.Commit;
import org.openlca.util.Strings;

public class DiffEntries {

	public static List<DiffEntry> workspace(Config config) throws IOException {
		return workspace(config, null, null);
	}

	public static List<DiffEntry> workspace(Config config, Commit commit) throws IOException {
		return workspace(config, commit, null);
	}

	public static List<DiffEntry> workspace(Config config, Commit commit, List<String> paths) throws IOException {
		var walk = new TreeWalk(config.repo);
		var commitOid = commit != null ? ObjectId.fromString(commit.id) : null;
		if (commitOid == null) {
			commitOid = config.repo.resolve(Constants.HEAD);
		}
		var revCommit = commitOid != null ? config.repo.parseCommit(commitOid) : null;
		if (commit == null) {
			walk.addTree(new EmptyTreeIterator());
		} else {
			walk.addTree(revCommit.getTree().getId());

		}
		walk.addTree(new DatabaseIterator(config));
		if (paths != null) {
			walk.setFilter(getPathsFilter(paths.stream().distinct().toList()));
		}
		walk.setRecursive(true);
		return DiffEntry.scan(walk);
	}

	private static TreeFilter getPathsFilter(List<String> paths) {
		if (paths.isEmpty())
			return null;
		TreeFilter filter = null;
		for (var path : paths) {
			if (Strings.nullOrEmpty(path))
				continue;
			var newFilter = PathFilter.create(path);
			filter = filter != null ? AndTreeFilter.create(filter, newFilter) : newFilter;
		}
		return filter;
	}

}