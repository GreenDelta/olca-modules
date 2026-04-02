package org.openlca.io.olca;

import java.util.Objects;

import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NwSet;

final class ImpactMethodTransfer implements EntityTransfer<ImpactMethod> {

	private final TransferConfig conf;

	ImpactMethodTransfer(TransferConfig conf) {
		this.conf = conf;
	}

	@Override
	public void syncAll() {
		for (var d : conf.source().getDescriptors(ImpactMethod.class)) {
			var origin = conf.source().get(ImpactMethod.class, d.id);
			sync(origin);
		}
	}

	@Override
	public ImpactMethod sync(ImpactMethod origin) {
		return conf.sync(origin, () -> {
			var copy = origin.copy();
			copy.source = conf.swap(origin.source);

			// swap impact categories
			copy.impactCategories.clear();
			for (var impact : origin.impactCategories) {
				var swapped = conf.swap(impact);
				if (swapped != null) {
					copy.impactCategories.add(swapped);
				}
			}

			// swap impact categories in NW-sets
			for (var copied : copy.nwSets) {
				for (var f : copied.factors) {
					f.impactCategory = conf.swap(f.impactCategory);
				}
			}
			for (var nwSet : origin.nwSets) {
				for (var copied : copy.nwSets) {
					// we need to set the reference IDs from the source as they are
					// generated new in the clone method.
					if (areEqual(copied, nwSet)) {
						copied.refId = nwSet.refId;
						break;
					}
				}
			}

			return copy;
		});
	}

	private boolean areEqual(NwSet source, NwSet target) {
		return Objects.equals(source.name, target.name)
			&& Objects.equals(source.description, target.description)
			&& Objects.equals(source.weightedScoreUnit, target.weightedScoreUnit);
	}

}
