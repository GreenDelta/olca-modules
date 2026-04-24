package org.openlca.io.olca;

import org.openlca.core.model.ImpactCategory;

final class ImpactCategoryTransfer implements EntityTransfer<ImpactCategory> {

	private final TransferContext ctx;

	ImpactCategoryTransfer(TransferContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void syncAll() {
		for (var d : ctx.source().getDescriptors(ImpactCategory.class)) {
			var origin = ctx.source().get(ImpactCategory.class, d.id);
			sync(origin);
		}
	}

	@Override
	public ImpactCategory sync(ImpactCategory origin) {
		return ctx.sync(origin, () -> {
			var copy = origin.copy();
			copy.source = ctx.resolve(origin.source);
			for (var f : copy.impactFactors) {
				f.flow = ctx.resolve(f.flow);
				f.flowPropertyFactor = ctx.mapFactor(f.flow, f.flowPropertyFactor);
				f.unit = f.unit != null
						? ctx.mapUnit(f.flowPropertyFactor, f.unit)
						: null;
				f.location = ctx.resolve(f.location);
			}
			return copy;
		});
	}
}
