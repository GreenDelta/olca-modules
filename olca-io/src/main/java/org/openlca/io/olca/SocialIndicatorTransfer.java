package org.openlca.io.olca;

import org.openlca.core.model.SocialIndicator;

final class SocialIndicatorTransfer implements EntityTransfer<SocialIndicator> {

	private final TransferConfig conf;

	SocialIndicatorTransfer(TransferConfig conf) {
		this.conf = conf;
	}

	@Override
	public void syncAll() {
		for (var d : conf.source().getDescriptors(SocialIndicator.class)) {
			var origin = conf.source().get(SocialIndicator.class, d.id);
			sync(origin);
		}
	}

	@Override
	public SocialIndicator sync(SocialIndicator origin) {
		return conf.sync(origin, () -> {
			var copy = origin.copy();
			copy.activityQuantity = conf.swap(origin.activityQuantity);
			copy.activityUnit = conf.mapUnit(copy.activityQuantity, origin.activityUnit);
			return copy;
		});
	}
}