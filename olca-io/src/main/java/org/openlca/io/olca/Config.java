package org.openlca.io.olca;

import java.util.function.Function;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Unit;
import org.slf4j.LoggerFactory;

record Config(
		IDatabase source,
		IDatabase target,
		SeqMap seq,
		ImportLog log) {

	static Config of(IDatabase source, IDatabase target) {
		var seq = SeqMap.create(source, target);
		var log = new ImportLog();
		return new Config(source, target, seq, log);
	}

	<T extends RootEntity> void syncAll(Class<T> type, Function<T, T> fn) {
		if (type == null || fn == null)
			return;
		var seqType = ModelType.of(type);
		if (seqType == null) {
			log.error("unknown type " + type.getName());
			return;
		}
		for (var d : source.getDescriptors(type)) {
			if (isMapped(seqType, d.id)) {
				log.skipped(d);
				continue;
			}
			var e = source.get(type, d.id);
			copy(e, fn);
		}
	}

	boolean isMapped(ModelType seqType,long sourceId) {
		return seq.isMapped(seqType, sourceId);
	}

	<T extends RootEntity> T copy(T entity, Function<T, T> fn) {
		if (entity == null)
			return null;
		var copied = fn.apply(entity);
		if (copied == null)
			return null;
		copied.refId = entity.refId;
		copied.category = swap(entity.category);
		copied = target.insert(copied);
		log.imported(copied);
		var seqType = ModelType.of(copied);
		if (seqType == null)
			return copied;
		seq.put(seqType, entity.id, copied.id);
		return copied;
	}

	@SuppressWarnings("unchecked")
	<T extends RootEntity> T swap(T sourceEntity) {
		if (sourceEntity == null)
			return null;
		var clazz = sourceEntity.getClass();
		var type = ModelType.of(clazz);
		long id = seq.get(type, sourceEntity.id);
		if (id == 0) {
			log.error("could not find " + clazz
					+ " " + sourceEntity.refId);
			return null;
		}
		return (T) target.get(clazz, id);
	}

	static Unit findUnit(FlowPropertyFactor fac, String unitRefId) {
		return fac != null
				? findUnit(fac.flowProperty, unitRefId)
				: null;
	}

	static Unit findUnit(FlowProperty prop, String unitRefId) {
		if (prop == null || prop.unitGroup == null || unitRefId == null)
			return null;

		// first check the reference unit, because this is often requested
		var refUnit = prop.getReferenceUnit();
		if (refUnit != null && unitRefId.equals(refUnit.refId))
			return refUnit;

		// then check the other units in the group
		for (var u : prop.unitGroup.units) {
			if (unitRefId.equals(u.refId))
				return u;
		}

		LoggerFactory.getLogger(Config.class)
				.error("could not fiend unit {} in flow property {}", unitRefId, prop);
		return null;
	}
}
