package org.openlca.io.olca;

import org.openlca.core.model.FlowProperty;

final class FlowPropertyTransfer implements EntityTransfer<FlowProperty> {

	private final TransferConfig conf;

	FlowPropertyTransfer(TransferConfig conf) {
		this.conf = conf;
	}

	@Override
	public void syncAll() {
		for (var property : conf.source().getAll(FlowProperty.class)) {
			sync(property);
		}
	}

	@Override
	public FlowProperty sync(FlowProperty origin) {
		return conf.sync(origin, () -> {
			var copy = origin.copy();
			copy.unitGroup = conf.swap(origin.unitGroup);
			return copy;
		});
	}
}
