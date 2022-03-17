package org.openlca.git.find;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.openlca.core.model.ModelType;
import org.openlca.git.util.GitUtil;

class ModelFilter extends TreeFilter {

	private final ModelType type;
	private final String refId;

	ModelFilter(ModelType type, String refId) {
		this.type = type;
		this.refId = refId;
	}

	@Override
	public boolean include(TreeWalk walk)
			throws MissingObjectException, IncorrectObjectTypeException, IOException {
		if (walk.getFileMode() == FileMode.TREE)
			return walk.getPathString().startsWith(type.name());
		var name = walk.getNameString();
		return name.equals(refId + GitUtil.DATASET_SUFFIX);
	}

	@Override
	public boolean shouldBeRecursive() {
		return true;
	}

	@Override
	public TreeFilter clone() {
		return new ModelFilter(type, refId);
	}

}