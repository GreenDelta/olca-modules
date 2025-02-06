package org.openlca.git.util;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.core.model.ModelType;

public class TypedRefIdSet {

	private final EnumMap<ModelType, Set<String>> map = new EnumMap<>(ModelType.class);

	public TypedRefIdSet() {
	}

	public TypedRefIdSet(Collection<? extends TypedRefId> refs) {
		addAll(refs);
	}

	public void add(TypedRefId pair) {
		add(pair.type, pair.refId);
	}

	public void add(ModelType type, String refId) {
		map.computeIfAbsent(type, t -> new HashSet<>()).add(refId);
	}

	public void addAll(TypedRefIdSet pairs) {
		pairs.forEach(ref -> add(ref));
	}

	public void addAll(Collection<? extends TypedRefId> pairs) {
		pairs.forEach(this::add);
	}

	public boolean contains(TypedRefId pair) {
		return contains(pair.type, pair.refId);
	}

	public boolean contains(ModelType type, String refId) {
		var refIds = map.get(type);
		if (refIds == null)
			return false;
		return refIds.contains(refId);
	}

	public void remove(TypedRefId pair) {
		remove(pair.type, pair.refId);
	}

	public void remove(ModelType type, String refId) {
		var refIds = map.get(type);
		if (refIds == null)
			return;
		refIds.remove(refId);
	}

	public void clear() {
		map.clear();
	}

	public void forEach(Consumer<TypedRefId> forEach) {
		map.keySet().forEach(type -> {
			var refIds = map.get(type);
			if (refIds == null)
				return;
			refIds.forEach(refId -> forEach.accept(new TypedRefId(type, refId)));
		});
	}

	public void forEach(ForEach forEach) {
		map.keySet().forEach(type -> {
			var refIds = map.get(type);
			if (refIds == null)
				return;
			refIds.forEach(refId -> forEach.accept(type, refId));
		});
	}

	public Set<String> get(ModelType type) {
		var refIds = map.get(type);
		if (refIds == null)
			return new HashSet<>();
		return refIds;
	}

	public Set<String> refIds() {
		var refIds = new HashSet<String>();
		map.values().forEach(set -> refIds.addAll(set));
		return refIds;
	}

	public Set<ModelType> types() {
		return new HashSet<>(map.keySet());
	}

	public ModelType getTypeFor(String refId) {
		for (var type : map.keySet())
			if (map.get(type) != null && map.get(type).contains(refId))
				return type;
		return null;
	}

	public interface ForEach {

		void accept(ModelType type, String refId);

	}
}
