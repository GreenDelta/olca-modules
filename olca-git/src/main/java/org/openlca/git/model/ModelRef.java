package org.openlca.git.model;

import org.openlca.commons.Strings;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.TypedRefId;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.util.GitUtil;

public class ModelRef extends TypedRefId implements Comparable<ModelRef> {

	public final String path;
	public final String name;
	public final String category;
	public final boolean isModelType;
	public final boolean isCategory;
	public final boolean isEmptyCategory;
	public final boolean isDataset;
	public final boolean isRepositoryInfo;
	public final boolean isLibrary;

	public ModelRef(String path) {
		super(getType(path), getRefId(path));
		path = trimPaths(path);
		this.isEmptyCategory = GitUtil.isEmptyCategoryPath(path);
		if (this.isEmptyCategory) {
			path = path.substring(0, path.length() - GitUtil.EMPTY_CATEGORY_FLAG.length() - 1);
		}
		this.path = path;
		this.name = nameOf(path);
		this.category = categoryOf(type, path);
		this.isModelType = type != null && !path.contains("/");
		this.isCategory = type != null && path.contains("/") && Strings.isBlank(refId);
		this.isDataset = path.contains("/") && refId != null && !path.startsWith(RepositoryInfo.FILE_NAME + "/");
		this.isRepositoryInfo = RepositoryInfo.FILE_NAME.equals(path);
		this.isLibrary = path.startsWith(RepositoryInfo.FILE_NAME + "/");
	}

	public ModelRef(ModelRef ref) {
		super(ref.type, ref.refId);
		this.path = ref.path;
		this.name = ref.name;
		this.category = ref.category;
		this.isModelType = ref.isModelType;
		this.isCategory = ref.isCategory;
		this.isEmptyCategory = ref.isEmptyCategory;
		this.isDataset = ref.isDataset;
		this.isRepositoryInfo = ref.isRepositoryInfo;
		this.isLibrary = ref.isLibrary;
	}

	private static ModelType getType(String path) {
		if (path.isEmpty())
			return null;
		for (var type : ModelType.values())
			if (type.name().equals(path.split("/")[0].trim()))
				return type;
		return null;
	}

	private static String getRefId(String path) {
		var parts = path.split("/");
		if (parts.length < 2)
			return null;
		if (parts[0].equals(RepositoryInfo.FILE_NAME))
			return parts[1];
		var binDir = GitUtil.findBinDir(path);
		if (binDir != null)
			return GitUtil.getRefId(binDir);
		return GitUtil.getRefId(path);
	}

	private static String nameOf(String path) {
		return path.contains("/")
				? path.substring(path.lastIndexOf("/") + 1)
				: path;
	}

	private static String categoryOf(ModelType type, String path) {
		if (type == null || !path.contains("/"))
			return "";
		var p = path.substring(path.indexOf("/") + 1);
		if (!p.contains("/"))
			return "";
		return p.substring(0, p.lastIndexOf("/"));
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
		var p1 = path.split("/");
		var p2 = o.path.split("/");
		for (var i = 0; i < Math.min(p1.length, p2.length); i++) {
			// encode to ensure same order as in Git tree
			var c = compare(GitUtil.encode(p1[i]), GitUtil.encode(p2[i]));
			if (c != 0)
				return c;
		}
		return p1.length - p2.length;
	}

	private int compare(String p1, String p2) {
		var isP1Tree = !GitUtil.isDatasetPath(p1);
		var isP2Tree = !GitUtil.isDatasetPath(p2);
		if (isP1Tree) {
			p1 += "/";
		}
		if (isP2Tree) {
			p2 += "/";
		}
		return p1.compareTo(p2);
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
		return s + ", path=" + path + ", category=" + category + ", name=" + name;
	}
}
