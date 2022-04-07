package org.openlca.git.util;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.model.ModelType;

public class TypeRefIdMap<T> {

	private final EnumMap<ModelType, Map<String, T>> map = new EnumMap<>(ModelType.class);

	public void put(ModelType type, String refId, T value) {
		map.computeIfAbsent(type, t -> new HashMap<>()).put(refId, value);
	}

	public boolean contains(ModelType type, String refId) {
		var refIds = map.get(type);
		if (refIds == null)
			return false;
		return refIds.containsKey(refId);
	}

	public T get(ModelType type, String refId) {
		var refIds = map.get(type);
		if (refIds == null)
			return null;
		return refIds.get(refId);
	}

	public void clear() {
		map.clear();
	}

	public boolean isEmpty() {
		if (map.isEmpty())
			return true;
		for (Map<String, T> inner : map.values())
			if (inner != null && !inner.isEmpty())
				return false;
		return true;
	}

}
