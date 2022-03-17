package org.openlca.git.find;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.openlca.git.util.GitUtil;

public class NotBinaryFilter extends TreeFilter {

	public static NotBinaryFilter create() {
		return new NotBinaryFilter();
	}

	private NotBinaryFilter() {
	}

	@Override
	public TreeFilter clone() {
		return this;
	}

	@Override
	public boolean include(TreeWalk walk) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		if (walk.getFileMode() != FileMode.TREE)
			return true;
		var path = walk.getPathString();
		return !GitUtil.isBinDir(path);
	}

	@Override
	public boolean shouldBeRecursive() {
		return false;
	}

}
