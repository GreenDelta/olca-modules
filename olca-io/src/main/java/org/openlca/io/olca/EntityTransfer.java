package org.openlca.io.olca;

import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;

public sealed interface EntityTransfer<T extends RootEntity> permits
	CategoryTransfer,
	CurrencyTransfer,
	DefaultTransfer,
	FlowPropertyTransfer,
	ResultTransfer,
	UnitGroupTransfer {

	void syncAll();

	T sync(T sourceEntity);

	@SuppressWarnings("unchecked")
	static <T extends RootEntity> T call(T sourceEntity, TransferConfig config) {
		return (T) switch (sourceEntity) {
			case null -> null;
			case Category c -> new CategoryTransfer(config).sync(c);
			case Currency c -> new CurrencyTransfer(config).sync(c);
			case FlowProperty p -> new FlowPropertyTransfer(config).sync(p);
			case Result r -> new ResultTransfer(config).sync(r);
			case UnitGroup u -> new UnitGroupTransfer(config).sync(u);
			default ->
				new DefaultTransfer<>(config, (Class<T>) sourceEntity.getClass())
					.sync(sourceEntity);
		};
	}
}
