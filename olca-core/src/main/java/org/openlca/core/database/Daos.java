package org.openlca.core.database;

import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.MappingFile;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessGroupSet;

public class Daos {

	@SuppressWarnings("unchecked")
	public static <T extends AbstractEntity> BaseDao<T> base(IDatabase db, Class<T> clazz) {
		if (db == null || clazz == null)
			return null;
		var type = ModelType.forModelClass(clazz);
		if (type != null)
			return (BaseDao<T>) refDao(db, type);
		if (clazz == Exchange.class)
			return (BaseDao<T>) new ExchangeDao(db);
		if (clazz == MappingFile.class)
			return (BaseDao<T>) new MappingFileDao(db);
		if (clazz == ProcessGroupSet.class)
			return (BaseDao<T>) new ProcessGroupSetDao(db);
		return new BaseDao<>(clazz, db);
	}

	public static RefEntityDao<?, ?> refDao(IDatabase db, ModelType type) {
		if (db == null | type == null)
			return null;
		if (type.isRoot())
			return root(db, type);
		if (type == ModelType.NW_SET)
			return new NwSetDao(db);
		if (type == ModelType.UNIT)
			return new UnitDao(db);
		return null;
	}

	public static RootEntityDao<?, ?> root(
		IDatabase db, ModelType type) {
		if (db == null || type == null || !type.isRoot())
			return null;
		return switch (type) {
			case ACTOR -> new ActorDao(db);
			case CATEGORY -> new CategoryDao(db);
			case CURRENCY -> new CurrencyDao(db);
			case DQ_SYSTEM -> new DQSystemDao(db);
			case FLOW -> new FlowDao(db);
			case FLOW_PROPERTY -> new FlowPropertyDao(db);
			case IMPACT_METHOD -> new ImpactMethodDao(db);
			case IMPACT_CATEGORY -> new ImpactCategoryDao(db);
			case LOCATION -> new LocationDao(db);
			case PARAMETER -> new ParameterDao(db);
			case PROCESS -> new ProcessDao(db);
			case PRODUCT_SYSTEM -> new ProductSystemDao(db);
			case PROJECT -> new ProjectDao(db);
			case SOCIAL_INDICATOR -> new SocialIndicatorDao(db);
			case SOURCE -> new SourceDao(db);
			case UNIT_GROUP -> new UnitGroupDao(db);
			case RESULT -> new ResultDao(db);
			case EPD -> new EpdDao(db);
			default -> null;
		};
	}
}
