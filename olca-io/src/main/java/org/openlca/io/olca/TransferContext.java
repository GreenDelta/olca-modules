package org.openlca.io.olca;

import java.util.function.Supplier;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;

public class TransferContext {

	private final IDatabase source;
	private final IDatabase target;
	private final SeqMap seq;
	private final ImportLog log;

	private TransferContext(
		IDatabase source, IDatabase target, SeqMap seq, ImportLog log
	) {
		this.source = source;
		this.target = target;
		this.seq = seq;
		this.log = log;
	}

	IDatabase source() {
		return source;
	}

	IDatabase target() {
		return target;
	}

	SeqMap seq() {
		return seq;
	}

	public ImportLog log() {
		return log;
	}

	public static TransferContext create(IDatabase source, IDatabase target) {
		var seq = SeqMap.create(source, target);
		var log = new ImportLog();
		return new TransferContext(source, target, seq, log);
	}

	public EntityTransfer<?> getTransfer(ModelType type) {
		return switch(type) {
			case ACTOR -> new DefaultTransfer<>(this, Actor.class);
			case CATEGORY -> new CategoryTransfer(this);
			case CURRENCY -> new CurrencyTransfer(this);
			case DQ_SYSTEM -> new DqsTransfer(this);
			case EPD -> new EpdTransfer(this);
			case FLOW -> new FlowTransfer(this);
			case FLOW_PROPERTY -> new FlowPropertyTransfer(this);
			case IMPACT_CATEGORY -> new ImpactCategoryTransfer(this);
			case IMPACT_METHOD -> new ImpactMethodTransfer(this);
			case LOCATION -> new DefaultTransfer<>(this, Location.class);
			case PARAMETER -> new ParameterTransfer(this);
			case PROCESS -> new ProcessTransfer(this);
			case PRODUCT_SYSTEM -> new ProductSystemTransfer(this);
			case PROJECT -> new ProjectTransfer(this);
			case RESULT -> new ResultTransfer(this);
			case SOCIAL_INDICATOR -> new SocialIndicatorTransfer(this);
			case SOURCE -> new DefaultTransfer<>(this, Source.class);
			case UNIT_GROUP -> new UnitGroupTransfer(this);
			case null -> throw new IllegalArgumentException("type is null");
		};
	}

	@SuppressWarnings("unchecked")
	public <T extends RootEntity> EntityTransfer<T> getTransfer(Class<T> type) {
		if (type == null)
			throw new IllegalArgumentException("type is null");
		var modelType = ModelType.of(type);
		if (modelType == null)
			throw new IllegalArgumentException("unsupported type: " + type.getName());
		return (EntityTransfer<T>) getTransfer(modelType);
	}

	<T extends RootEntity> T save(long sourceId, T targetEntity) {
		if (targetEntity == null) return null;
		var saved = targetEntity.id == 0
			? target.insert(targetEntity)
			: target.update(targetEntity);
		seq.put(ModelType.of(targetEntity), sourceId, saved.id);
		return saved;
	}

	/// Get the corresponding mapped entity from the target database if it exists.
	/// Returns `null` if there is no mapping for that entity yet.
	@SuppressWarnings("unchecked")
	<T extends RootEntity> T getMapped(T sourceEntity) {
		if (sourceEntity == null)
			return null;
		var targetId = seq.get(ModelType.of(sourceEntity), sourceEntity.id);
		return targetId != 0
			? (T) target.get(sourceEntity.getClass(), targetId)
			: null;
	}

	<T extends RootEntity> T sync(T origin, Supplier<T> fn) {
		if (origin == null)	return null;
		var mapped = getMapped(origin);
		if (mapped != null) return mapped;
		var copy = fn.get();
		if (copy == null) return null;
		copy.refId = origin.refId;
		copy.category = swap(origin.category);
		copy = save(origin.id, copy);
		log.imported(copy);
		return copy;
	}

	@SuppressWarnings("unchecked")
	<T extends RootEntity> T swap(T sourceEntity) {
		if (sourceEntity == null) return null;
		var mapped = getMapped(sourceEntity);
		if (mapped != null) return mapped;
		var type = (Class<T>) sourceEntity.getClass();
		return getTransfer(type).sync(sourceEntity);
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
