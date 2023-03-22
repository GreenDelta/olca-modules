package org.openlca.git.model;

import org.openlca.git.util.TypedRefId;

public class ModelRef extends TypedRefId implements Comparable<ModelRef> {

	public final String path;
	public final String category;

	public ModelRef(String path) {
		super(path);
		this.path = path;
		this.category = getCategory(path);
	}

	public ModelRef(ModelRef ref) {
		super(ref.type, ref.refId);
		this.path = ref.path;
		this.category = ref.category;
	}

	public static String getCategory(String path) {
		path = path.substring(path.indexOf("/") + 1);
		if (!path.contains("/"))
			return "";
		return path.substring(0, path.lastIndexOf("/"));
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

}
