package org.openlca.io.olca;

import org.openlca.core.model.Epd;

final class EpdTransfer implements EntityTransfer<Epd> {

	private final TransferConfig conf;

	EpdTransfer(TransferConfig conf) {
		this.conf = conf;
	}

	@Override
	public void syncAll() {
		for (var d : conf.source().getDescriptors(Epd.class)) {
			var origin = conf.source().get(Epd.class, d.id);
			sync(origin);
		}
	}

	@Override
	public Epd sync(Epd origin) {
		return conf.sync(origin, () -> {
			var copy = origin.copy();
			copy.pcr = conf.swap(origin.pcr);
			copy.programOperator = conf.swap(origin.programOperator);
			copy.manufacturer = conf.swap(origin.manufacturer);
			copy.verifier = conf.swap(origin.verifier);
			copy.location = conf.swap(origin.location);
			copy.originalEpd = conf.swap(origin.originalEpd);
			copy.dataGenerator = conf.swap(origin.dataGenerator);
			if (copy.product != null && origin.product != null) {
				var p = copy.product;
				var originProduct = origin.product;
				p.flow = conf.swap(originProduct.flow);
				p.property = conf.swap(originProduct.property);
				p.unit = conf.mapUnit(p.property, originProduct.unit);
			}
			for (var mod : copy.modules) {
				mod.result = conf.swap(mod.result);
			}
			return copy;
		});
	}
}