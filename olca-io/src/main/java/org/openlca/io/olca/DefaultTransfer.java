package org.openlca.io.olca;

import org.openlca.core.model.RootEntity;

final class DefaultTransfer<T extends RootEntity>
	implements EntityTransfer<T> {

	private final TransferConfig config;
	private final Class<T> type;

	DefaultTransfer(TransferConfig config, Class<T> type) {
		this.config = config;
		this.type = type;
	}

	@Override
	public void syncAll() {
		for (var d : config.source().getDescriptors(type)) {
			var origin = config.source().get(type, d.id);
			sync(origin);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public T sync(T origin) {
		return config.sync(origin, () -> (T) origin.copy());
	}
}
