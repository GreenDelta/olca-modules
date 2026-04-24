package org.openlca.io.olca;

import org.openlca.core.model.DQSystem;

final class DqsTransfer implements EntityTransfer<DQSystem> {

	private final TransferContext ctx;

	DqsTransfer(TransferContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void syncAll() {
		for (var d : ctx.source().getDescriptors(DQSystem.class)) {
			var origin = ctx.source().get(DQSystem.class, d.id);
			sync(origin);
		}
	}

	@Override
	public DQSystem sync(DQSystem origin) {
		return ctx.sync(origin, () -> {
			var copy = origin.copy();
			copy.source = ctx.resolve(origin.source);
			return copy;
		});
	}
}
