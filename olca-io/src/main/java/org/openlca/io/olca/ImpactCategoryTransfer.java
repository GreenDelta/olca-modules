package org.openlca.io.olca;

import org.openlca.core.model.ImpactCategory;

final class ImpactCategoryTransfer implements EntityTransfer<ImpactCategory> {

	private final TransferConfig conf;

	ImpactCategoryTransfer(TransferConfig conf) {
		this.conf = conf;
	}

	@Override
	public void syncAll() {
		for (var d : conf.source().getDescriptors(ImpactCategory.class)) {
			var origin = conf.source().get(ImpactCategory.class, d.id);
			sync(origin);
		}
	}

	@Override
	public ImpactCategory sync(ImpactCategory origin) {
		return conf.sync(origin, () -> {
			var copy = origin.copy();
			copy.source = conf.swap(origin.source);
			for (var f : copy.impactFactors) {
				f.flow = conf.swap(f.flow);
				f.flowPropertyFactor = conf.mapFactor(f.flow, f.flowPropertyFactor);
				f.unit = f.unit != null
						? conf.mapUnit(f.flowPropertyFactor, f.unit)
						: null;
				f.location = conf.swap(f.location);
			}
			return copy;
		});
	}
}
