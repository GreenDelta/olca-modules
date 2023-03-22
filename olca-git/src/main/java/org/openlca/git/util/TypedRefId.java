package org.openlca.git.util;

import java.util.Objects;

import org.openlca.core.model.ModelType;
import org.openlca.util.Strings;

public class TypedRefId {

	public final ModelType type;
	public final String refId;

	public TypedRefId(String path) {
		this.type = getType(path);
		this.refId = getRefId(path);
	}
	
	public TypedRefId(ModelType type, String refId) {
		this.type = type;
		this.refId = refId;
	}
	
	private ModelType getType(String path) {
		if (path.isEmpty())
			return null;
		return ModelType.valueOf(path.split("/")[0]);
	}

	private static String getRefId(String path) {
		var parts = path.split("/");
		var last = parts[parts.length - 1];
		if (last.endsWith(GitUtil.DATASET_SUFFIX))
			return last.substring(last.lastIndexOf("/") + 1, last.indexOf(GitUtil.DATASET_SUFFIX));
		return null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, refId);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TypedRefId))
			return false;
		var o = (TypedRefId) obj;
		return type == o.type && Strings.nullOrEqual(refId, o.refId);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[type=" + type + ", refId=" + refId + "]";
	}

}
