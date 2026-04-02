package org.openlca.io.olca;

import org.openlca.core.model.RootEntity;

final class DefaultTransfer<T extends RootEntity>
	implements EntityTransfer<T> {

	private final TransferContext ctx;
	private final Class<T> type;

	DefaultTransfer(TransferContext ctx, Class<T> type) {
		this.ctx = ctx;
		this.type = type;
	}

	@Override
	public void syncAll() {
		for (var d : ctx.source().getDescriptors(type)) {
			var origin = ctx.source().get(type, d.id);
			sync(origin);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public T sync(T origin) {
		return ctx.sync(origin, () -> (T) origin.copy());
	}
}
