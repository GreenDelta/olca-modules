package org.openlca.core.model.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.RootDescriptor;

public class InMemoryStore implements EntityStore {

	private final HashMap<Class<?>, ConcurrentHashMap<Long, RootEntity>> store;
	private final ConcurrentHashMap<String, Long> refSeq;
	private final AtomicLong idSeq = new AtomicLong(0);

	private InMemoryStore() {
		var types = ModelType.values();
		store = new HashMap<>(types.length);
		for (var type : types) {
			store.put(type.getModelClass(), new ConcurrentHashMap<>());
		}
		refSeq = new ConcurrentHashMap<>();
	}

	public static InMemoryStore create() {
		return new InMemoryStore();
	}

	@Override
	public <T extends RootEntity> T insert(T e) {
		if (e == null)
			return null;
		e.id = idSeq.incrementAndGet();
		var map = store.get(e.getClass());
		map.put(e.id, e);
		if (e.refId != null) {
			refSeq.put(e.refId, e.id);
		}

		// handle child categories
		if (e instanceof Category c) {
			for (var child : c.childCategories) {
				if (child.id == 0) {
					insert(child);
				}
			}
		}
		return e;
	}

	@Override
	public <T extends RootEntity> T update(T e) {
		if (e == null)
			return null;
		var map = store.get(e.getClass());
		var old = map.put(e.id, e);

		// the ref-id could have change
		if (old == null) {
			if (e.refId != null) {
				refSeq.put(e.refId, e.id);
			}
		} else {
			if (!Objects.equals(old.refId, e.refId)) {
				if (old.refId != null) {
					refSeq.remove(old.refId);
				}
				if (e.refId != null) {
					refSeq.put(e.refId, e.id);
				}
			}
		}

		// handle child categories
		if (e instanceof Category c) {
			for (var child : c.childCategories) {
				if (child.id == 0) {
					insert(child);
				} else {
					update(child);
				}
			}
		}
		return e;
	}

	@Override
	public <T extends RootEntity> void delete(T e) {
		if (e == null)
			return;
		var map = store.get(e.getClass());
		map.remove(e.id);
		if (e instanceof Category c) {
			for (var child : c.childCategories) {
				delete(child);
			}
		}
	}

	@Override
	public <T extends RootEntity> T get(Class<T> type, long id) {
		if (type == null)
			return null;
		var map = store.get(type);
		return type.cast(map.get(id));
	}

	@Override
	public <T extends RootEntity> T get(Class<T> type, String refId) {
		if (type == null || refId == null)
			return null;
		var map = store.get(type);
		Long id = refSeq.get(refId);
		if (id != null) {
			var e = map.get(id);
			if (e != null)
				return type.cast(e);
		}
		for (var e : map.values()) {
			if (Objects.equals(e.refId, refId))
				return type.cast(e);
		}
		return null;
	}

	@Override
	public <T extends RootEntity> RootDescriptor getDescriptor(
			Class<T> type, long id) {
		var e = get(type, id);
		return e != null
				? Descriptor.of(e)
				: null;
	}

	@Override
	public <T extends RootEntity> RootDescriptor getDescriptor(
			Class<T> type, String refId) {
		var e = get(type, refId);
		return e != null
				? Descriptor.of(e)
				: null;
	}

	@Override
	public <T extends RootEntity> List<T> getAll(Class<T> type) {
		var map = store.get(type);
		var values = map.values();
		var list = new ArrayList<T>(values.size());
		for (var e : values) {
			list.add(type.cast(e));
		}
		return list;
	}

	@Override
	public <T extends RootEntity> List<? extends RootDescriptor> getDescriptors(
			Class<T> type) {
		var map = store.get(type);
		var values = map.values();
		var list = new ArrayList<RootDescriptor>(values.size());
		for (var e : values) {
			list.add(Descriptor.of(e));
		}
		return list;
	}

	@Override
	public <T extends RootEntity> T getForName(Class<T> type, String name) {
		var map = store.get(type);
		for (var e : map.values()) {
			if (Objects.equals(e.name, name))
				return type.cast(e);
		}
		return null;
	}

	@Override
	public void clear() {
		for (var type : ModelType.values()) {
			var map = store.get(type.getModelClass());
			map.clear();
		}
		refSeq.clear();
	}

}
