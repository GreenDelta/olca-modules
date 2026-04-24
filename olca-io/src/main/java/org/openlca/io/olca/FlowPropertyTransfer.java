package org.openlca.io.olca;

import org.openlca.core.model.FlowProperty;

final class FlowPropertyTransfer implements EntityTransfer<FlowProperty> {

	private final TransferContext ctx;

	FlowPropertyTransfer(TransferContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void syncAll() {
		for (var property : ctx.source().getAll(FlowProperty.class)) {
			sync(property);
		}
	}

	@Override
	public FlowProperty sync(FlowProperty origin) {
		return ctx.sync(origin, () -> {
			var copy = origin.copy();
			copy.unitGroup = ctx.resolve(origin.unitGroup);
			return copy;
		});
	}
}
