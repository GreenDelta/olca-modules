package org.openlca.git.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.openlca.git.GitConfig;
import org.openlca.git.find.NotBinaryFilter;
import org.openlca.git.iterator.DatabaseIterator;
import org.openlca.git.model.Commit;
import org.openlca.jsonld.SchemaVersion;
import org.openlca.util.Strings;

public class DiffEntries {

	public static List<DiffEntry> workspace(GitConfig config) throws IOException {
		return workspace(config, null, null);
	}

	public static List<DiffEntry> workspace(GitConfig config, Commit commit) throws IOException {
		return workspace(config, commit, null);
	}

	public static List<DiffEntry> workspace(GitConfig config, Commit commit, List<String> paths) throws IOException {
		var walk = new TreeWalk(config.repo);
		var commitOid = commit != null ? ObjectId.fromString(commit.id) : null;
		var revCommit = commitOid != null ? config.repo.parseCommit(commitOid) : Repositories.headCommitOf(config.repo);
		if (commit == null) {
			walk.addTree(new EmptyTreeIterator());
		} else {
			walk.addTree(revCommit.getTree().getId());

		}
		walk.addTree(new DatabaseIterator(config));
		if (paths == null) {
			paths = new ArrayList<>();
		}
		walk.setFilter(getPathsFilter(paths.stream().distinct().toList()));
		walk.setRecursive(true);
		return DiffEntry.scan(walk);
	}

	private static TreeFilter getPathsFilter(List<String> paths) {
		var filter = PathFilter.create(SchemaVersion.FILE_NAME).negate();
		filter = AndTreeFilter.create(filter, NotBinaryFilter.create());
		if (paths.isEmpty())
			return filter;
		for (var path : paths) {
			if (Strings.nullOrEmpty(path))
				continue;
			filter = AndTreeFilter.create(filter, PathFilter.create(path));
		}
		return filter;
	}

}
