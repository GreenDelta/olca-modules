package org.openlca.core.model;

import java.util.Objects;

import org.openlca.util.Strings;

public class TypedRefId {

	public final ModelType type;
	public final String refId;

	public TypedRefId(ModelType type, String refId) {
		this.type = type;
		this.refId = refId;
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
