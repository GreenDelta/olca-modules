package org.openlca.io.olca;

import org.openlca.core.model.RootEntity;

public sealed interface EntityTransfer<T extends RootEntity> permits
	CategoryTransfer,
	CurrencyTransfer,
	DefaultTransfer,
	DqsTransfer,
	EpdTransfer,
	FlowTransfer,
	FlowPropertyTransfer,
	ImpactCategoryTransfer,
	ImpactMethodTransfer,
	ParameterTransfer,
	ProcessTransfer,
	ProjectTransfer,
	ProductSystemTransfer,
	ResultTransfer,
	SocialIndicatorTransfer,
	UnitGroupTransfer {

	void syncAll();

	T sync(T sourceEntity);
}
