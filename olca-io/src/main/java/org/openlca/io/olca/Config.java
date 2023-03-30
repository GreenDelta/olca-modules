package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

import java.util.function.Function;

record Config(
		IDatabase source,
		IDatabase target,
		Seq seq,
		ImportLog log) {

	static Config of(IDatabase source, IDatabase target) {
		var seq = new Seq(target);
		var log = new ImportLog();
		return new Config(source, target, seq, log);
	}

	<T extends RootEntity> void syncAll(Class<T> type, Function<T, T> fn) {
		if (type == null || fn == null)
			return;
		int seqType = seqTypeOf(type);
		if (seqType < 0) {
			log.error("unknown type " + type.getName());
			return;
		}
		for (var d : source.getDescriptors(type)) {
			if (contains(seqType, d.refId))
				continue;
			var e = source.get(type, d.id);
			copy(e, fn);
		}
	}

	boolean contains(int seqType, String refId) {
		return seq.contains(seqType, refId);
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
		int seqType = seqTypeOf(copied.getClass());
		if (seqType < 0)
			return copied;
		seq.put(seqType, entity.refId, copied.id);
		return copied;
	}

	@SuppressWarnings("unchecked")
	<T extends RootEntity> T swap(T sourceEntity) {
		if (sourceEntity == null)
			return null;
		var type = sourceEntity.getClass();
		int seqType = seqTypeOf(type);
		long id = seq.get(seqType, sourceEntity.refId);
		return id == 0
				? null
				: (T) target.get(type, id);
	}

	private <T extends RefEntity> int seqTypeOf(Class<T> type) {
		if (type == null)
			return -1;
		if (type.equals(Category.class))
			return Seq.CATEGORY;
		if (type.equals(Location.class))
			return Seq.LOCATION;
		if (type.equals(Actor.class))
			return Seq.ACTOR;
		if (type.equals(Source.class))
			return Seq.SOURCE;
		if (type.equals(Unit.class))
			return Seq.UNIT;
		if (type.equals(UnitGroup.class))
			return Seq.UNIT_GROUP;
		if (type.equals(FlowProperty.class))
			return Seq.FLOW_PROPERTY;
		if (type.equals(Flow.class))
			return Seq.FLOW;
		if (type.equals(Currency.class))
			return Seq.CURRENCY;
		if (type.equals(Process.class))
			return Seq.PROCESS;
		if (type.equals(ProductSystem.class))
			return Seq.PRODUCT_SYSTEM;
		if (type.equals(ImpactCategory.class))
			return Seq.IMPACT_CATEGORY;
		if (type.equals(ImpactMethod.class))
			return Seq.IMPACT_METHOD;
		if (type.equals(NwSet.class))
			return Seq.NW_SET;
		if (type.equals(Project.class))
			return Seq.PROJECT;
		if (type.equals(DQSystem.class))
			return Seq.DQ_SYSTEM;
		if (type.equals(SocialIndicator.class))
			return Seq.SOCIAL_INDICATOR;
		if (type.equals(Result.class))
			return Seq.RESULT;
		if (type.equals(Epd.class))
			return Seq.EPD;
		return -1;
	}

}
