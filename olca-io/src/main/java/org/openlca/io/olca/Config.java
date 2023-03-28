package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
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
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

import java.util.function.Function;

record Config(
		IDatabase source,
		IDatabase target,
		Sequence seq,
		ImportLog log) {

	static Config of(IDatabase source, IDatabase target) {
		var seq = new Sequence(target);
		var log = new ImportLog();
		return new Config(source, target, seq, log);
	}

	<T extends RootEntity> void syncAll(Class<T> type, Function<T, T> copy) {
		if (type == null || copy == null)
			return;
		int seqType = seqTypeOf(type);
		if (seqType < 0) {
			log.error("unknown type " + type.getName());
			return;
		}
		for (var d : source.getDescriptors(type)) {
			if (seq.contains(seqType, d.refId))
				continue;
			var e = source.get(type, d.id);
			if (e == null)
				continue;
			var copied = copy.apply(e);
			if (copied == null)
				continue;
			copied.refId = e.refId;
			copied.category = swap(e.category);
			copied = target.insert(copied);
			seq.put(seqType, e.refId, copied.id);
		}
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
			return 0;
		if (type.equals(Location.class))
			return 1;
		if (type.equals(Actor.class))
			return 2;
		if (type.equals(Source.class))
			return 3;
		if (type.equals(Unit.class))
			return 4;
		if (type.equals(UnitGroup.class))
			return 5;
		if (type.equals(FlowProperty.class))
			return 6;
		if (type.equals(Flow.class))
			return 7;
		if (type.equals(Currency.class))
			return 8;
		if (type.equals(Process.class))
			return 9;
		if (type.equals(ProductSystem.class))
			return 10;
		if (type.equals(ImpactCategory.class))
			return 11;
		if (type.equals(ImpactMethod.class))
			return 12;
		if (type.equals(NwSet.class))
			return 13;
		if (type.equals(Project.class))
			return 14;
		if (type.equals(DQSystem.class))
			return 15;
		if (type.equals(SocialIndicator.class))
			return 16;
		return -1;
	}

}
