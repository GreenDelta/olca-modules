package org.openlca.proto.io.input;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.jsonld.input.UpdateMode;

class ImportCache {

	private final ProtoImport imp;

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

	ImportCache(ProtoImport imp) {
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

	@SuppressWarnings("unchecked")
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

		var proto = (ProtoBox<?, T>)readProto(modelType, refId);
		if (proto == null) {
			if (model == null)
				return ImportItem.error();
			visited(model);
			return ImportItem.visited(model);
		}

		if (skipImport(model, proto)) {
			visited(model);
			return ImportItem.visited(model);
		}

		return model == null
			? ImportItem.newOf(proto)
			: ImportItem.update( proto, model);
	}

	private <T extends RootEntity> boolean skipImport(T model, ProtoBox<?, T> proto) {
		if (model == null || imp.updateMode == UpdateMode.ALWAYS)
			return false;
		long jsonVersion = Util.versionOf(proto);
		if (jsonVersion != model.version)
			return jsonVersion < model.version;
		long jsonDate = Util.lastChangeOf(proto);
		return jsonDate <= model.lastChange;
	}

	private ProtoBox<?, ?> readProto(ModelType type, String refId) {
		if (type == null)
			return null;
		return switch (type) {
			case PROJECT -> {
				var proto = imp.reader.getProject(refId);
				yield proto != null ? ProtoBox.of(proto) : null;
			}
			case IMPACT_METHOD -> {
				var proto = imp.reader.getImpactMethod(refId);
				yield proto != null ? ProtoBox.of(proto) : null;
			}
			case IMPACT_CATEGORY -> {
				var proto = imp.reader.getImpactCategory(refId);
				yield proto != null ? ProtoBox.of(proto) : null;
			}
			case PRODUCT_SYSTEM -> {
				var proto = imp.reader.getProductSystem(refId);
				yield proto != null ? ProtoBox.of(proto) : null;
			}
			case PROCESS -> {
				var proto = imp.reader.getProcess(refId);
				yield proto != null ? ProtoBox.of(proto) : null;
			}
			case FLOW -> {
				var proto = imp.reader.getFlow(refId);
				yield proto != null ? ProtoBox.of(proto) : null;
			}
			case FLOW_PROPERTY -> {
				var proto = imp.reader.getFlowProperty(refId);
				yield proto != null ? ProtoBox.of(proto) : null;
			}
			case UNIT_GROUP -> {
				var proto = imp.reader.getUnitGroup(refId);
				yield proto != null ? ProtoBox.of(proto) : null;
			}
			case ACTOR -> {
				var proto = imp.reader.getActor(refId);
				yield proto != null ? ProtoBox.of(proto) : null;
			}
			case SOURCE -> {
				var proto = imp.reader.getSource(refId);
				yield proto != null ? ProtoBox.of(proto) : null;
			}
			case LOCATION -> {
				var proto = imp.reader.getLocation(refId);
				yield proto != null ? ProtoBox.of(proto) : null;
			}
			case SOCIAL_INDICATOR -> {
				var proto = imp.reader.getSocialIndicator(refId);
				yield proto != null ? ProtoBox.of(proto) : null;
			}
			case CURRENCY -> {
				var proto = imp.reader.getCurrency(refId);
				yield proto != null ? ProtoBox.of(proto) : null;
			}
			case PARAMETER -> {
				var proto = imp.reader.getParameter(refId);
				yield proto != null ? ProtoBox.of(proto) : null;
			}
			case DQ_SYSTEM -> {
				var proto = imp.reader.getDQSystem(refId);
				yield proto != null ? ProtoBox.of(proto) : null;
			}
			case RESULT -> {
				var proto = imp.reader.getResult(refId);
				yield proto != null ? ProtoBox.of(proto) : null;
			}
			case EPD -> {
				var proto = imp.reader.getEpd(refId);
				yield proto != null ? ProtoBox.of(proto) : null;
			}
			default -> null;
		};
	}

}
