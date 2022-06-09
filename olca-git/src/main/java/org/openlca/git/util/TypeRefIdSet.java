package org.openlca.git.util;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.core.model.ModelType;

public class TypeRefIdSet {

	private final EnumMap<ModelType, Set<String>> map = new EnumMap<>(ModelType.class);

	public TypeRefIdSet() {
	}

	public TypeRefIdSet(Collection<? extends TypeRefIdPair> refs) {
		refs.forEach(this::add);
	}

	public void add(TypeRefIdPair pair) {
		map.computeIfAbsent(pair.type, t -> new HashSet<>()).add(pair.refId);
	}

	public boolean contains(TypeRefIdPair pair) {
		var refIds = map.get(pair.type);
		if (refIds == null)
			return false;
		return refIds.contains(pair.refId);
	}

	public void remove(TypeRefIdPair pair) {
		var refIds = map.get(pair.type);
		if (refIds == null)
			return;
		refIds.remove(pair.refId);
	}

	public void clear() {
		map.clear();
	}

	public void forEach(Consumer<TypeRefIdPair> forEach) {
		map.keySet().forEach(type -> {
			var refIds = map.get(type);
			if (refIds == null)
				return;
			refIds.forEach(refId -> forEach.accept(new TypeRefIdPair(type, refId)));
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

}
