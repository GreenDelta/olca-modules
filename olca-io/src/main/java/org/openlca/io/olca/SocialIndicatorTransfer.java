package org.openlca.io.olca;

import org.openlca.core.model.SocialIndicator;

final class SocialIndicatorTransfer implements EntityTransfer<SocialIndicator> {

	private final TransferContext ctx;

	SocialIndicatorTransfer(TransferContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void syncAll() {
		for (var d : ctx.source().getDescriptors(SocialIndicator.class)) {
			var origin = ctx.source().get(SocialIndicator.class, d.id);
			sync(origin);
		}
	}

	@Override
	public SocialIndicator sync(SocialIndicator origin) {
		return ctx.sync(origin, () -> {
			var copy = origin.copy();
			copy.activityQuantity = ctx.resolve(origin.activityQuantity);
			copy.activityUnit = ctx.mapUnit(copy.activityQuantity, origin.activityUnit);
			return copy;
		});
	}
}
