package org.openlca.git.model;

import org.openlca.git.util.GitUtil;
import org.openlca.git.util.TypedRefId;
import org.openlca.util.Strings;

public class ModelRef extends TypedRefId implements Comparable<ModelRef> {

	public final String path;
	public final String category;
	public final boolean isCategory;
	public final boolean isEmptyCategory;

	public ModelRef(String path) {
		super(path);
		path = trimPaths(path);
		this.isEmptyCategory = path.endsWith("/" + GitUtil.EMPTY_CATEGORY_FLAG);
		if (this.isEmptyCategory) {
			path = path.substring(0, path.indexOf("/" + GitUtil.EMPTY_CATEGORY_FLAG));
		}
		this.isCategory = path.contains("/") && Strings.nullOrEmpty(refId);
		this.category = getCategory(path);
		this.path = path;
	}

	public ModelRef(ModelRef ref) {
		super(ref.type, ref.refId);
		this.path = ref.path;
		this.category = ref.category;
		this.isCategory = ref.isCategory;
		this.isEmptyCategory = ref.isEmptyCategory;
	}

	private String getCategory(String path) {
		if (!path.contains("/"))
			return "";
		path = path.substring(path.indexOf("/") + 1);
		if (!path.contains("/"))
			return "";
		return path.substring(0, path.lastIndexOf("/"));
	}

	public String getCategoryPath() {
		if (!isCategory)
			return category;
		return path.substring(path.indexOf("/") + 1);
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ModelRef))
			return false;
		var other = (ModelRef) o;
		return path.equals(other.path);
	}

	@Override
	public int compareTo(ModelRef o) {
		return path.compareTo(o.path);
	}

	private String trimPaths(String path) {
		while (path.contains(" /")) {
			path = path.replace(" /", "/");
		}
		while (path.contains("/ ")) {
			path = path.replace("/ ", "/");
		}
		return path;
	}

	@Override
	protected String fieldsToString() {
		var s = super.fieldsToString();
		return s + ", path=" + path + ", category=" + category + ", isCategory=" + isCategory + ", isEmptyCategory="
				+ isEmptyCategory;
	}

}
