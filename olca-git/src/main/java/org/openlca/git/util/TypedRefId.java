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
		for (var type : ModelType.values())
			if (type.name().equals(path.split("/")[0].trim()))
				return type;
		return null;
	}

	private static String getRefId(String path) {
		var parts = path.split("/");
		if (parts.length < 2)
			return null;
		var binDir = GitUtil.findBinDir(path);
		if (binDir != null)
			return GitUtil.getRefId(binDir);
		return GitUtil.getRefId(path);
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

	protected String fieldsToString() {
		return "type=" + type + ", refId=" + refId;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + fieldsToString() + "]";
	}

}
