package org.openlca.git.iterator;

import java.io.File;

import org.eclipse.jgit.lib.FileMode;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.git.util.GitUtil;

class TreeEntry implements Comparable<TreeEntry> {

	public final String name;
	public final FileMode fileMode;
	public final Object data;
	public final File file;

	TreeEntry(ModelType type) {
		this(type.name(), FileMode.TREE, type);
	}

	TreeEntry(Category category) {
		this(GitUtil.encode(category.name), FileMode.TREE, category);
	}

	TreeEntry(RootDescriptor descriptor) {
		this(descriptor.refId + GitUtil.DATASET_SUFFIX, FileMode.REGULAR_FILE, descriptor);
	}

	TreeEntry(String name, FileMode fileMode) {
		this(name, fileMode, null);
	}

	TreeEntry(String name, FileMode fileMode, Object data) {
		this(name, fileMode, data, null);
	}

	TreeEntry(String name, FileMode fileMode, Object data, File file) {
		this.name = name;
		this.fileMode = fileMode;
		this.data = data;
		this.file = file;
	}

	@Override
	public int compareTo(TreeEntry e) {
		return getName().compareTo(e.getName());
	}

	private String getName() {
		if (fileMode == FileMode.TREE)
			return name + "/";
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

}
