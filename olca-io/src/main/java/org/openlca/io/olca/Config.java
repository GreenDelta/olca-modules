package org.openlca.io.olca;

import java.util.function.Function;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Unit;

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

	boolean isMapped(ModelType seqType, long sourceId) {
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

	/// Returns the corresponding flow property factor of the destination flow.
	FlowPropertyFactor mapFactor(Flow destFlow, FlowPropertyFactor srcFactor) {
		if (srcFactor == null || destFlow == null)
			return null;
		FlowProperty srcProp = srcFactor.flowProperty;
		if (srcProp == null)
			return null;
		long propId = seq.get(ModelType.FLOW_PROPERTY, srcProp.id);
		for (FlowPropertyFactor fac : destFlow.flowPropertyFactors) {
			if (fac.flowProperty == null)
				continue;
			if (propId == fac.flowProperty.id)
				return fac;
		}

		log.error("could not find flow property "
				+ srcFactor.flowProperty.refId + " in flow " + destFlow.refId);
		return null;
	}

	Unit mapUnit(FlowPropertyFactor destFac, Unit srcUnit) {
		return destFac != null
				? mapUnit(destFac.flowProperty, srcUnit)
				: null;
	}

	Unit mapUnit(FlowProperty destProp, Unit srcUnit) {
		if (destProp == null
				|| destProp.unitGroup == null
				|| srcUnit == null)
			return null;

		// first check the reference unit, because this is often requested
		var refUnit = destProp.getReferenceUnit();

		// first, try to find it by refId
		var refId = srcUnit.refId;
		if (refId != null) {
			if (refUnit != null && refId.equals(refUnit.refId))
				return refUnit;
			for (var u : destProp.unitGroup.units) {
				if (refId.equals(u.refId))
					return u;
			}
		}

		// then, try to find it by name
		var u = destProp.unitGroup.getUnit(srcUnit.name);
		if (u != null)
			return u;

		log.error("could not find unit " + srcUnit.name
				+ " in flow property " + destProp.name);
		return null;
	}
}
