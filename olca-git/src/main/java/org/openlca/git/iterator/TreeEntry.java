package org.openlca.git.iterator;

import org.eclipse.jgit.lib.FileMode;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.git.util.GitUtil;

class TreeEntry implements Comparable<TreeEntry> {

	public final String name;
	public final FileMode fileMode;
	public final Object data;

	TreeEntry(ModelType type) {
		this(type.name(), FileMode.TREE, type);
	}

	TreeEntry(Category category) {
		this(GitUtil.encode(category.name), FileMode.TREE, category);
	}

	TreeEntry(CategorizedDescriptor descriptor, boolean asProto) {
		this(descriptor.refId + (asProto ? ".proto" : ".json"), FileMode.REGULAR_FILE, descriptor);
	}

	TreeEntry(String name, FileMode fileMode) {
		this(name, fileMode, null);
	}

	TreeEntry(String name, FileMode fileMode, Object data) {
		this.name = name;
		this.fileMode = fileMode;
		this.data = data;
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