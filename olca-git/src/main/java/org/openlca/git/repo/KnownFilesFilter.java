package org.openlca.git.repo;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.openlca.core.model.ModelType;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.util.GitUtil;
import org.openlca.util.Strings;

class KnownFilesFilter extends TreeFilter {

	private final Integer depth;
	private boolean includeEmptyCategoryTags;
	
	private KnownFilesFilter(Integer depth) {
		this.depth = depth;
	}

	public static KnownFilesFilter create() {
		return new KnownFilesFilter(null);
	}

	public static KnownFilesFilter createForPath(String path) {
		return new KnownFilesFilter(getDepth(path));
	}

	public KnownFilesFilter includeEmptyCategoryTags() {
		this.includeEmptyCategoryTags = true;
		return this;
	}
	
	private static int getDepth(String path) {
		if (Strings.nullOrEmpty(path))
			return 0;
		var p = path;
		var depth = 1;
		while (p.contains("/")) {
			p = p.substring(p.indexOf("/") + 1);
			depth++;
		}
		return depth;
	}
	private boolean isRecognizedRootFile(String path, FileMode mode, int depth) {
		if (mode != FileMode.REGULAR_FILE || depth > 0)
			return false;
		return path.equals(RepositoryInfo.FILE_NAME);
	}

	private boolean isModelTypeRootDirectory(String path, FileMode mode, int depth) {
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

	private boolean isCategory(String path, FileMode mode, int depth) {
		return mode == FileMode.TREE && depth > 0;
	}

	private boolean isDataset(String path, FileMode mode, int depth) {
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

	private boolean isBinDir(String path, FileMode mode, int depth) {
		if (mode != FileMode.TREE || depth < 1)
			return false;
		if (path.contains("/")) {
			path = path.substring(path.lastIndexOf("/") + 1);
		}
		return GitUtil.isBinDir(path);
	}

	private boolean isEmptyCategoryTag(String path, FileMode mode, int depth) {
		if (mode != FileMode.REGULAR_FILE || depth < 1)
			return false;
		return path.equals(GitUtil.EMPTY_CATEGORY_FLAG) || path.endsWith("/" + GitUtil.EMPTY_CATEGORY_FLAG);
	}

	@Override
	public boolean include(TreeWalk walker) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		var path = GitUtil.decode(walker.getPathString());
		var mode = walker.getFileMode();
		var depth = this.depth != null ? this.depth : walker.getDepth();
		return include(path, mode, depth);
	}
	
	private boolean include(String path, FileMode mode, int depth) {
		if (isBinDir(path, mode, depth))
			return false;
		if (isCategory(path, mode, depth))
			return true;
		if (isModelTypeRootDirectory(path, mode, depth))
			return true;
		if (isRecognizedRootFile(path, mode, depth))
			return false;
		if (isEmptyCategoryTag(path, mode, depth))
			return includeEmptyCategoryTags;
		if (isDataset(path, mode, depth))
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
