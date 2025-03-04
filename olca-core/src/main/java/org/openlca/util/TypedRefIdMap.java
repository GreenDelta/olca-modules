package org.openlca.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.TypedRefId;

public class TypedRefIdMap<T> {

	private final EnumMap<ModelType, Map<String, T>> map = new EnumMap<>(ModelType.class);

	public TypedRefIdMap<T> put(TypedRefId pair, T value) {
		return put(pair.type, pair.refId, value);
	}

	public TypedRefIdMap<T> put(ModelType type, String refId, T value) {
		// using LinkedHashMap to keep order and reduce sorting time of big maps
		map.computeIfAbsent(type, t -> new LinkedHashMap<>()).put(refId, value);
		return this;
	}

	public TypedRefIdMap<T> putAll(TypedRefIdMap<T> map) {
		map.forEach((type, refId, resolution) -> put(type, refId, resolution));
		return this;
	}

	public boolean contains(TypedRefId pair) {
		return contains(pair.type, pair.refId);
	}

	public boolean contains(ModelType type, String refId) {
		var refIds = map.get(type);
		if (refIds == null)
			return false;
		return refIds.containsKey(refId);
	}

	public T get(TypedRefId pair) {
		return get(pair.type, pair.refId);
	}

	public T get(ModelType type, String refId) {
		var refIds = map.get(type);
		if (refIds == null)
			return null;
		return refIds.get(refId);
	}

	public List<T> get(ModelType type) {
		var refIds = map.get(type);
		if (refIds == null)
			return new ArrayList<>();
		return new ArrayList<>(refIds.values());
	}

	public TypedRefIdSet keySet() {
		var keys = new TypedRefIdSet();
		map.keySet().forEach(type -> {
			var refIds = map.get(type);
			if (refIds == null || refIds.isEmpty())
				return;
			refIds.keySet().forEach(refId -> {
				keys.add(new TypedRefId(type, refId));
			});
		});
		return keys;
	}

	public List<T> values() {
		return map.values().stream().map(Map::values).flatMap(Collection::stream).collect(Collectors.toList());
	}

	public int size() {
		int total = 0;
		for (var map : this.map.values()) {
			total += map.values().size();
		}
		return total;
	}

	public T remove(TypedRefId pair) {
		return remove(pair.type, pair.refId);
	}

	public T remove(ModelType type, String refId) {
		var refIds = map.get(type);
		if (refIds == null)
			return null;
		return refIds.remove(refId);
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

	public void forEach(ForEach<T> consumer) {
		map.keySet().forEach(type -> {
			var refIds = map.get(type);
			if (refIds == null || refIds.isEmpty())
				return;
			refIds.keySet().forEach(refId -> {
				consumer.accept(type, refId, refIds.get(refId));
			});
		});
	}

	public interface ForEach<T> {

		void accept(ModelType type, String refId, T value);

	}

}
