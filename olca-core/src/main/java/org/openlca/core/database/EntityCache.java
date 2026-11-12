package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.Descriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/// A loading cache for entities and descriptors. It is intended for descriptors
/// and for rarely changing entities that are used very often (like categories,
/// locations, or flow properties), e.g. for rendering labels.
///
/// Note the following when using this cache:
///
/// - Entities returned from this cache are shared between all callers. They
///   must be treated as read-only and must never be modified or persisted.
///   Load an entity from the database when it should be changed.
/// - The cache is not synchronized with the database automatically. Code that
///   modifies the database is responsible to invalidate the affected entries.
/// - The cache holds at most [#MAX_SIZE] entries; when this limit is reached,
///   entries that were not used recently are evicted.
///
/// Prefer descriptors to full entities when only general information like names
/// or categories is needed.
public class EntityCache {

	/// The maximum number of entries the cache holds.
	public static final int MAX_SIZE = 100_000;

	public final IDatabase db;
	private final Logger log = LoggerFactory.getLogger(getClass());
	/// Missing entities are cached as empty optionals, because Guava caches
	/// do not support `null` values.
	private final LoadingCache<Key, Optional<Object>> cache;

	public static EntityCache create(IDatabase db) {
		return new EntityCache(db);
	}

	private EntityCache(IDatabase db) {
		this.db = db;
		cache = CacheBuilder.newBuilder()
			.maximumSize(MAX_SIZE)
			.build(new Loader(db));
	}

	public <T> T get(Class<T> clazz, long id) {
		try {
			return cache.get(Key.get(clazz, id))
				.map(clazz::cast)
				.orElse(null);
		} catch (Exception e) {
			log.error("failed to get from cache {} with id {}", clazz, id, e);
			return null;
		}
	}

	public <T> Map<Long, T> getAll(Class<T> clazz, Collection<Long> ids) {
		List<Key> keys = new ArrayList<>(ids.size());
		for (long id : ids) {
			keys.add(Key.get(clazz, id));
		}
		try {
			Map<Key, Optional<Object>> values = cache.getAll(keys);
			Map<Long, T> result = new HashMap<>(values.size());
			values.forEach((key, value) -> value.ifPresent(
				obj -> result.put(key.id, clazz.cast(obj))));
			return result;
		} catch (Exception e) {
			log.error("failed to get entities from cache: {}", clazz, e);
			return Collections.emptyMap();
		}
	}

	public void invalidate(Class<?> clazz, long id) {
		cache.invalidate(Key.get(clazz, id));
	}

	public void refresh(Class<?> clazz, long id) {
		cache.refresh(Key.get(clazz, id));
	}

	public void invalidateAll() {
		cache.invalidateAll();
	}

	public void invalidateAll(Class<?> clazz, Collection<Long> ids) {
		List<Key> keys = new ArrayList<>(ids.size());
		for (long id : ids) {
			keys.add(Key.get(clazz, id));
		}
		cache.invalidateAll(keys);
	}

	private record Key(Class<?> clazz, long id) {

		private Key {
			Objects.requireNonNull(clazz);
		}

		private static Key get(Class<?> clazz, long id) {
			return new Key(clazz, id);
		}
	}

	private static class Loader extends CacheLoader<Key, Optional<Object>> {

		private final Logger log = LoggerFactory.getLogger(getClass());
		private final IDatabase db;
		private final Map<Class<?>, BaseDao<?>> entityDaos;
		private final Map<Class<?>, RefEntityDao<?, ?>> descriptorDaos;

		public Loader(IDatabase db) {
			this.db = db;
			var entityDaos = new HashMap<Class<?>, BaseDao<?>>();
			var descriptorDaos = new HashMap<Class<?>, RefEntityDao<?, ?>>();
			for (var modelType : ModelType.values()) {
				if (modelType.getModelClass() == null)
					continue;
				var dao = Daos.root(db, modelType);
				if (dao != null) {
					entityDaos.put(modelType.getModelClass(), dao);
					descriptorDaos.put(dao.getDescriptorType(), dao);
				}
			}
			this.entityDaos = Map.copyOf(entityDaos);
			this.descriptorDaos = Map.copyOf(descriptorDaos);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Map<Key, Optional<Object>> loadAll(Iterable<? extends Key> keys) {
			Multimap<Class<?>, Long> idMap = ArrayListMultimap.create();
			for (Key key : keys)
				idMap.put(key.clazz, key.id);
			Set<Class<?>> classes = idMap.keySet();
			HashMap<Key, Object> found = new HashMap<>();
			for (Class<?> clazz : classes) {
				Collection<Long> ids = idMap.get(clazz);
				if (ids.isEmpty())
					continue;
				if (Descriptor.class.isAssignableFrom(clazz)) {
					loadDescriptors(clazz, ids, found);
				} else {
					loadFullEntities((Class<? extends AbstractEntity>) clazz,
						ids, found);
				}
			}
			HashMap<Key, Optional<Object>> result = new HashMap<>();
			for (Key key : keys) {
				result.put(key, Optional.ofNullable(found.get(key)));
			}
			return result;
		}

		private void loadFullEntities(
			Class<? extends AbstractEntity> clazz,
			Collection<Long> ids,
			HashMap<Key,
				Object> result
		) {
			BaseDao<?> dao = getDao(clazz);
			List<?> entities = dao.getForIds(new HashSet<>(ids));
			for (Object obj : entities) {
				AbstractEntity entity = (AbstractEntity) obj;
				result.put(Key.get(clazz, entity.id), entity);
			}
		}

		private void loadDescriptors(
			Class<?> clazz, Collection<Long> ids, HashMap<Key, Object> result
		) {
			RefEntityDao<?, ?> dao = descriptorDaos.get(clazz);
			if (dao == null) {
				log.error("unknown descriptor class {}, returning null", clazz);
				return;
			}
			List<? extends Descriptor> descriptors = dao
				.getDescriptors(new HashSet<>(ids));
			for (Descriptor descriptor : descriptors)
				result.put(Key.get(clazz, descriptor.id), descriptor);
		}

		@Override
		public Optional<Object> load(Key key) {
			var obj = Descriptor.class.isAssignableFrom(key.clazz)
				? loadDescriptor(key)
				: loadFull(key);
			return Optional.ofNullable(obj);
		}

		private Object loadDescriptor(Key key) {
			RefEntityDao<?, ?> dao = descriptorDaos.get(key.clazz);
			if (dao == null) {
				log.error("unknown descriptor class {}, returning null", key.clazz);
				return null;
			}
			return dao.getDescriptor(key.id);
		}

		private Object loadFull(Key key) {
			@SuppressWarnings("unchecked")
			BaseDao<?> dao = getDao((Class<? extends AbstractEntity>) key.clazz);
			return dao.getForId(key.id);
		}

		/// Returns the DAO for the given entity class. For non-root entities,
		/// like units or exchanges, a new DAO is created on each call, which is
		/// cheap compared to the database query that follows.
		private BaseDao<?> getDao(Class<? extends AbstractEntity> clazz) {
			var dao = entityDaos.get(clazz);
			return dao != null
				? dao
				: Daos.base(db, clazz);
		}
	}

}
