package org.openlca.git.model;

import org.openlca.core.model.ModelType;
import org.openlca.git.util.GitUtil;

public class ModelRef implements Comparable<ModelRef> {

	public final String path;
	public final ModelType type;
	public final String refId;
	public final String category;

	public ModelRef(String path) {
		this.path = path;
		this.type = getModelType(path.contains("/")
				? path.substring(0, path.indexOf("/"))
				: path);
		path = path.substring(path.indexOf("/") + 1);
		this.category = path.contains("/") ? path.substring(0, path.lastIndexOf("/")) : "";
		this.refId = path.endsWith(GitUtil.DATASET_SUFFIX)
				? path.substring(
						path.contains("/")
								? path.lastIndexOf("/") + 1
								: 0,
						path.lastIndexOf("."))
				: null;
	}

	public ModelRef(ModelRef ref) {
		this.path = ref.path;
		this.type = ref.type;
		this.refId = ref.refId;
		this.category = ref.category;
	}

	private static ModelType getModelType(String type) {
		for (var modelType : ModelType.values())
			if (modelType.name().equals(type))
				return modelType;
		return null;
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
