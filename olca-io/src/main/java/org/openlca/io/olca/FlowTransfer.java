package org.openlca.io.olca;

import org.openlca.core.model.Flow;

final class FlowTransfer implements EntityTransfer<Flow> {

	private final TransferConfig conf;

	FlowTransfer(TransferConfig conf) {
		this.conf = conf;
	}

	@Override
	public void syncAll() {
		for (var d : conf.source().getDescriptors(Flow.class)) {
			var origin = conf.source().get(Flow.class, d.id);
			sync(origin);
		}
	}

	@Override
	public Flow sync(Flow origin) {
		return conf.sync(origin, () -> {
			var copy = origin.copy();
			copy.location = conf.swap(origin.location);
			copy.referenceFlowProperty = conf.swap(origin.referenceFlowProperty);
			for (var fac : copy.flowPropertyFactors) {
				fac.flowProperty = conf.swap(fac.flowProperty);
			}
			return copy;
		});
	}
}
