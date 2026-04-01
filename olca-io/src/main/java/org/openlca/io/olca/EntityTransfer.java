package org.openlca.io.olca;

import org.openlca.core.model.Category;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;

public sealed interface EntityTransfer<T extends RootEntity> permits
	CategoryTransfer,
	UnitGroupTransfer {

	void syncAll();

	T sync(T sourceEntity);

	@SuppressWarnings("unchecked")
	static <T extends RootEntity> T call(T sourceEntity, TransferConfig config) {
		return (T) switch (sourceEntity) {
			case null -> null;
			case Category c -> new CategoryTransfer(config).sync(c);
			case UnitGroup u -> new UnitGroupTransfer(config).sync(u);
			default -> throw new IllegalArgumentException(
				"No transfer registered for: " + sourceEntity);
		};
	}
}
