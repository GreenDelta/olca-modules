package org.openlca.io.olca;

import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.UnitGroup;

public sealed interface EntityTransfer<T extends RootEntity> permits
	CategoryTransfer,
	CurrencyTransfer,
	DefaultTransfer,
	EpdTransfer,
	FlowTransfer,
	FlowPropertyTransfer,
	ImpactCategoryTransfer,
	ImpactMethodTransfer,
	ParameterTransfer,
	ProcessTransfer,
	ResultTransfer,
	SocialIndicatorTransfer,
	UnitGroupTransfer {

	void syncAll();

	T sync(T sourceEntity);

	@SuppressWarnings("unchecked")
	static <T extends RootEntity> T call(T sourceEntity, TransferConfig config) {
		return (T) switch (sourceEntity) {
			case null -> null;
			case Category c -> new CategoryTransfer(config).sync(c);
			case Currency c -> new CurrencyTransfer(config).sync(c);
			case Epd e -> new EpdTransfer(config).sync(e);
			case Flow f -> new FlowTransfer(config).sync(f);
			case FlowProperty p -> new FlowPropertyTransfer(config).sync(p);
			case ImpactCategory i -> new ImpactCategoryTransfer(config).sync(i);
			case ImpactMethod i -> new ImpactMethodTransfer(config).sync(i);
			case Parameter p -> new ParameterTransfer(config).sync(p);
			case Process p -> new ProcessTransfer(config).sync(p);
			case Result r -> new ResultTransfer(config).sync(r);
			case SocialIndicator s -> new SocialIndicatorTransfer(config).sync(s);
			case UnitGroup u -> new UnitGroupTransfer(config).sync(u);
			default ->
				new DefaultTransfer<>(config, (Class<T>) sourceEntity.getClass())
					.sync(sourceEntity);
		};
	}
}
