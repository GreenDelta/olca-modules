package org.openlca.jsonld.input;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;

class ImportCache {

	private final JsonImport imp;

	private final Map<Class<?>, Map<String, Object>> cache = new HashMap<>();

	// small independent instances that we cache as full objects, instances
	// of classes that are not in this set are cached as descriptors
	private final Set<Class<?>> fullyCached = Set.of(
		Actor.class,
		Currency.class,
		DQSystem.class,
		Flow.class,
		FlowProperty.class,
		Location.class,
		SocialIndicator.class,
		Source.class,
		UnitGroup.class
	);

	ImportCache(JsonImport imp) {
		this.imp = imp;
	}

	void visited(RootEntity entity) {
		if (entity == null)
			return;
		var type = entity.getClass();
		var cacheMap = cache.computeIfAbsent(type, t -> new HashMap<>());
		var cacheObj = fullyCached.contains(type)
			? entity
			: Descriptor.of(entity);
		cacheMap.put(entity.refId, cacheObj);
	}

	Descriptor getDescriptor(Class<?> type, String refId) {
		var cacheMap = cache.get(type);
		if (cacheMap == null)
			return null;
		var cacheObj = cacheMap.get(refId);
		if (cacheObj == null)
			return null;
		if (cacheObj instanceof Descriptor d)
			return d;
		if (cacheObj instanceof RootEntity e)
			return Descriptor.of(e);
		return null;
	}

	<T extends RootEntity> ImportItem<T> fetch(Class<T> type, String refId) {
		if (type == null || refId == null)
			return ImportItem.error();
		var modelType = imp.types.get(type);
		if (modelType == null)
			return ImportItem.error();

		// first, try to read it from cache
		var cacheMap = cache.computeIfAbsent(type, t -> new HashMap<>());
		var cached = cacheMap.get(refId);
		if (cached != null) {
			if (type.isInstance(cached))
				return ImportItem.visited(type.cast(cached));
			if (cached instanceof Descriptor d) {
				var model = imp.db().get(type, d.id);
				if (model != null)
					return ImportItem.visited(model);
			}
		}

		// try to read it from the database
		T model = imp.db().get(type, refId);
		if (model != null) {
			if (imp.updateMode == UpdateMode.NEVER) {
				visited(model);
				return ImportItem.visited(model);
			}
		}

		var json = imp.reader.get(modelType, refId);
		if (json == null) {
			if (model == null)
				return ImportItem.error();
			visited(model);
			return ImportItem.visited(model);
		}

		if (skipImport(model, json)) {
			visited(model);
			return ImportItem.visited(model);
		}

		return model == null
			? ImportItem.newOf(json)
			: ImportItem.update(json, model);
	}

	private <T extends RefEntity> boolean skipImport(T model, JsonObject json) {
		if (model == null || imp.updateMode == UpdateMode.ALWAYS)
			return false;
		if (!(model instanceof RootEntity root))
			return false;
		long jsonVersion = Util.getVersion(json);
		if (jsonVersion != root.version)
			return jsonVersion < root.version;
		long jsonDate = Util.getLastChange(json);
		return jsonDate <= root.lastChange;
	}

}
