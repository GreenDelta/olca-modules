package org.openlca.io.olca;

import org.openlca.core.model.Epd;

final class EpdTransfer implements EntityTransfer<Epd> {

	private final TransferContext ctx;

	EpdTransfer(TransferContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void syncAll() {
		for (var d : ctx.source().getDescriptors(Epd.class)) {
			var origin = ctx.source().get(Epd.class, d.id);
			sync(origin);
		}
	}

	@Override
	public Epd sync(Epd origin) {
		return ctx.sync(origin, () -> {
			var copy = origin.copy();
			copy.pcr = ctx.swap(origin.pcr);
			copy.programOperator = ctx.swap(origin.programOperator);
			copy.manufacturer = ctx.swap(origin.manufacturer);
			copy.verifier = ctx.swap(origin.verifier);
			copy.location = ctx.swap(origin.location);
			copy.originalEpd = ctx.swap(origin.originalEpd);
			copy.dataGenerator = ctx.swap(origin.dataGenerator);
			if (copy.product != null && origin.product != null) {
				var p = copy.product;
				var originProduct = origin.product;
				p.flow = ctx.swap(originProduct.flow);
				p.property = ctx.swap(originProduct.property);
				p.unit = ctx.mapUnit(p.property, originProduct.unit);
			}
			for (var mod : copy.modules) {
				mod.result = ctx.swap(mod.result);
			}
			return copy;
		});
	}
}
