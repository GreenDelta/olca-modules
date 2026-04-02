package org.openlca.io.olca;

import java.util.Objects;

import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NwSet;

final class ImpactMethodTransfer implements EntityTransfer<ImpactMethod> {

	private final TransferContext ctx;

	ImpactMethodTransfer(TransferContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void syncAll() {
		for (var d : ctx.source().getDescriptors(ImpactMethod.class)) {
			var origin = ctx.source().get(ImpactMethod.class, d.id);
			sync(origin);
		}
	}

	@Override
	public ImpactMethod sync(ImpactMethod origin) {
		return ctx.sync(origin, () -> {
			var copy = origin.copy();
			copy.source = ctx.swap(origin.source);

			// swap impact categories
			copy.impactCategories.clear();
			for (var impact : origin.impactCategories) {
				var swapped = ctx.swap(impact);
				if (swapped != null) {
					copy.impactCategories.add(swapped);
				}
			}

			// swap impact categories in NW-sets
			for (var copied : copy.nwSets) {
				for (var f : copied.factors) {
					f.impactCategory = ctx.swap(f.impactCategory);
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
