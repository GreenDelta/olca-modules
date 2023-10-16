package org.openlca.git.find;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.openlca.core.model.ModelType;
import org.openlca.git.util.GitUtil;
import org.openlca.jsonld.PackageInfo;

public class KnownFilesFilter extends TreeFilter {

	private final Integer depth;
	
	public KnownFilesFilter() {
		depth = null;
	}

	public KnownFilesFilter(int depth) {
		this.depth = depth;
	}

	protected boolean isRecognizedRootFile(FileMode mode, String path, int depth) {
		if (mode != FileMode.REGULAR_FILE || depth > 0)
			return false;
		return path.equals(PackageInfo.FILE_NAME);
	}

	protected boolean isModelTypeRootDirectory(FileMode mode, String path, int depth) {
		if (mode != FileMode.TREE || depth > 0)
			return false;
		for (var type : ModelType.values()) {
			if (type == ModelType.CATEGORY)
				continue;
			if (type.name().equals(path))
				return true;
		}
		return false;
	}

	protected boolean isCategory(FileMode mode, String path, int depth) {
		return mode == FileMode.TREE && depth > 0;
	}

	protected boolean isDataset(FileMode mode, String path, int depth) {
		if (mode != FileMode.REGULAR_FILE || depth == 0)
			return false;
		if (!path.endsWith(GitUtil.DATASET_SUFFIX))
			return false;
		if (path.contains("/")) {
			path = path.substring(path.lastIndexOf("/") + 1);
		}
		path = path.substring(0, path.indexOf(GitUtil.DATASET_SUFFIX));
		return GitUtil.isUUID(path);
	}

	protected boolean isBinDir(FileMode mode, String path, int depth) {
		if (mode != FileMode.TREE || depth < 1)
			return false;
		if (path.contains("/")) {
			path = path.substring(path.lastIndexOf("/") + 1);
		}
		return GitUtil.isBinDir(path);
	}
	
	protected boolean isEmptyCategoryTag(FileMode mode, String path, int depth) {
		if (mode != FileMode.REGULAR_FILE || depth < 1)
			return false;
		return path.equals(GitUtil.EMPTY_CATEGORY_FLAG) || path.endsWith("/" + GitUtil.EMPTY_CATEGORY_FLAG);
	}

	@Override
	public boolean include(TreeWalk walker) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		var path = walker.getPathString();
		var mode = walker.getFileMode();
		var depth = this.depth != null
				? this.depth
				: walker.getDepth();
		if (isRecognizedRootFile(mode, path, depth))
			return false;
		if (isModelTypeRootDirectory(mode, path, depth))
			return true;
		if (isBinDir(mode, path, depth))
			return false;
		if (isCategory(mode, path, depth))
			return true;
		if (isDataset(mode, path, depth))
			return true;
		return false;
	}

	@Override
	public boolean shouldBeRecursive() {
		return depth != null;
	}

	@Override
	public TreeFilter clone() {
		return this;
	}

}
