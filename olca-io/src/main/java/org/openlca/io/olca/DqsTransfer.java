package org.openlca.io.olca;

import org.openlca.core.model.DQSystem;

final class DqsTransfer implements EntityTransfer<DQSystem> {

	private final TransferConfig conf;

	DqsTransfer(TransferConfig conf) {
		this.conf = conf;
	}

	@Override
	public void syncAll() {
		for (var d : conf.source().getDescriptors(DQSystem.class)) {
			var origin = conf.source().get(DQSystem.class, d.id);
			sync(origin);
		}
	}

	@Override
	public DQSystem sync(DQSystem origin) {
		return conf.sync(origin, () -> {
			var copy = origin.copy();
			copy.source = conf.swap(origin.source);
			return copy;
		});
	}
}