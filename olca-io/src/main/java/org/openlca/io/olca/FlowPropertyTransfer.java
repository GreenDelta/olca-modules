package org.openlca.io.olca;

import org.openlca.core.model.FlowProperty;

public final class FlowPropertyTransfer implements EntityTransfer<FlowProperty> {

	private final TransferConfig conf;

	public FlowPropertyTransfer(TransferConfig conf) {
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
		if (origin == null) return null;
		var mapped = conf.getMapped(origin);
		if (mapped != null) return mapped;

		var copy = origin.copy();
		copy.refId = origin.refId;
		copy.category = conf.swap(origin.category);
		copy.unitGroup = conf.swap(origin.unitGroup);
		return conf.save(origin.id, copy);
	}
}
