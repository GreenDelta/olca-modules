package org.openlca.core.database;

import org.openlca.core.model.ModelType;

public class Daos {

	public static RootEntityDao<?, ?> createRootDao(IDatabase database, ModelType type) {
		if (database == null)
			return null;
		if (type == null)
			return null;
		if (type.isCategorized())
			return createCategorizedDao(database, type);
		if (type == ModelType.IMPACT_CATEGORY)
			return new ImpactCategoryDao(database);
		if (type == ModelType.NW_SET)
			return new NwSetDao(database);
		if (type == ModelType.UNIT)
			return new UnitDao(database);
		return null;
	}

	public static  CategorizedEntityDao<?, ?> createCategorizedDao(IDatabase database,
			ModelType type) {
		if (database == null)
			return null;
		if (type == null)
			return null;
		if (!type.isCategorized())
			return null;
		switch (type) {
		case ACTOR:
			return new ActorDao(database);
		case COST_CATEGORY:
			return new CostCategoryDao(database);
		case CURRENCY:
			return new CurrencyDao(database);
		case FLOW:
			return new FlowDao(database);
		case FLOW_PROPERTY:
			return new FlowPropertyDao(database);
		case IMPACT_METHOD:
			return new ImpactMethodDao(database);
		case PROCESS:
			return new ProcessDao(database);
		case PRODUCT_SYSTEM:
			return new ProductSystemDao(database);
		case PROJECT:
			return new ProjectDao(database);
		case SOCIAL_INDICATOR:
			return new SocialIndicatorDao(database);
		case SOURCE:
			return new SourceDao(database);
		case UNIT_GROUP:
			return new UnitGroupDao(database);
		case LOCATION:
			return new LocationDao(database);
		case PARAMETER:
			return new ParameterDao(database);
		case CATEGORY:
			return new CategoryDao(database);
		default:
			return null;
		}
	}
}
