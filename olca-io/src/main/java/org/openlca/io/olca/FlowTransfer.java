package org.openlca.io.olca;

import org.openlca.core.model.Flow;

final class FlowTransfer implements EntityTransfer<Flow> {

	private final TransferContext ctx;

	FlowTransfer(TransferContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void syncAll() {
		for (var d : ctx.source().getDescriptors(Flow.class)) {
			var origin = ctx.source().get(Flow.class, d.id);
			sync(origin);
		}
	}

	@Override
	public Flow sync(Flow origin) {
		return ctx.sync(origin, () -> {
			var copy = origin.copy();
			copy.location = ctx.resolve(origin.location);
			copy.referenceFlowProperty = ctx.resolve(origin.referenceFlowProperty);
			for (var fac : copy.flowPropertyFactors) {
				fac.flowProperty = ctx.resolve(fac.flowProperty);
			}
			return copy;
		});
	}
}
