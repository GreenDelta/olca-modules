package org.openlca.git.model;

import java.util.Objects;

import org.openlca.core.model.ModelType;

public class ModelRef {

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
		this.refId = path.endsWith(".json")
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

	@Override
	public int hashCode() {
		return path != null ? path.hashCode() : super.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ModelRef))
			return false;
		var other = (ModelRef) o;
		return Objects.equals(path, other.path);
	}

	private static ModelType getModelType(String type) {
		for (var modelType : ModelType.values())
			if (modelType.name().equals(type))
				return modelType;
		return ModelType.UNKNOWN;
	}

}
