package org.openlca.git.iterator;

import org.eclipse.jgit.lib.FileMode;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.git.model.Change;
import org.openlca.git.util.GitUtil;

class TreeEntry implements Comparable<TreeEntry> {

	public final String name;
	public final FileMode fileMode;
	public final Object data;
	public final String filePath;

	public static TreeEntry empty() {
		return new TreeEntry(GitUtil.EMPTY_CATEGORY_FLAG, FileMode.REGULAR_FILE, null);
	}

	public static TreeEntry empty(Change change) {
		return new TreeEntry(GitUtil.EMPTY_CATEGORY_FLAG, FileMode.REGULAR_FILE, change);
	}

	TreeEntry(ModelType type) {
		this(type.name(), FileMode.TREE, type);
	}

	TreeEntry(Category category) {
		this(category.name.trim(), FileMode.TREE, category);
	}

	TreeEntry(Descriptor descriptor) {
		this(GitUtil.toDatasetFilename(descriptor.refId), FileMode.REGULAR_FILE, descriptor);
	}

	TreeEntry(String name, FileMode fileMode) {
		this(name, fileMode, null);
	}

	TreeEntry(String name, FileMode fileMode, Object data) {
		this(name, fileMode, data, null);
	}

	TreeEntry(String name, FileMode fileMode, Object data, String filePath) {
		this.name = GitUtil.encode(name);
		this.fileMode = fileMode;
		this.data = data;
		this.filePath = filePath;
	}

	@Override
	public int compareTo(TreeEntry e) {
		var n1 = name;
		if (fileMode == FileMode.TREE) {
			n1 += "/";
		}
		var n2 = e.name;
		if (e.fileMode == FileMode.TREE) {
			n2 += "/";
		}
		return n1.compareTo(n2);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

}
