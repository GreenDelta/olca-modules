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
			copy.pcr = ctx.resolve(origin.pcr);
			copy.programOperator = ctx.resolve(origin.programOperator);
			copy.manufacturer = ctx.resolve(origin.manufacturer);
			copy.verifier = ctx.resolve(origin.verifier);
			copy.location = ctx.resolve(origin.location);
			copy.originalEpd = ctx.resolve(origin.originalEpd);
			copy.dataGenerator = ctx.resolve(origin.dataGenerator);
			if (copy.product != null && origin.product != null) {
				var p = copy.product;
				var originProduct = origin.product;
				p.flow = ctx.resolve(originProduct.flow);
				p.property = ctx.resolve(originProduct.property);
				p.unit = ctx.mapUnit(p.property, originProduct.unit);
			}
			for (var mod : copy.modules) {
				mod.result = ctx.resolve(mod.result);
			}
			return copy;
		});
	}
}
