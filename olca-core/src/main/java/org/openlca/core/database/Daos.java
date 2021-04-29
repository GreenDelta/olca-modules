package org.openlca.core.database;

import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.MappingFile;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessGroupSet;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

public class Daos {

	@SuppressWarnings("unchecked")
	public static <T extends AbstractEntity> BaseDao<T> base(IDatabase database, Class<T> clazz) {
		if (database == null)
			return null;
		if (clazz == null)
			return null;
		var type = ModelType.forModelClass(clazz);
		if (type != null)
			return (BaseDao<T>) root(database, type);
		if (clazz == Exchange.class)
			return (BaseDao<T>) new ExchangeDao(database);
		if (clazz == MappingFile.class)
			return (BaseDao<T>) new MappingFileDao(database);
		if (clazz == ProcessGroupSet.class)
			return (BaseDao<T>) new ProcessGroupSetDao(database);
		return new BaseDao<>(clazz, database);
	}

	public static RootEntityDao<? extends RootEntity, ? extends Descriptor> root(
			IDatabase database, ModelType type) {
		if (database == null)
			return null;
		if (type == null)
			return null;
		if (type.isCategorized())
			return categorized(database, type);
		if (type == ModelType.NW_SET)
			return new NwSetDao(database);
		if (type == ModelType.UNIT)
			return new UnitDao(database);
		return null;
	}

	public static CategorizedEntityDao<? extends CategorizedEntity, ? extends CategorizedDescriptor> categorized(
			IDatabase database, ModelType type) {
		if (database == null)
			return null;
		if (type == null)
			return null;
		if (!type.isCategorized())
			return null;
		return switch (type) {
			case ACTOR -> new ActorDao(database);
			case CATEGORY -> new CategoryDao(database);
			case CURRENCY -> new CurrencyDao(database);
			case DQ_SYSTEM -> new DQSystemDao(database);
			case FLOW -> new FlowDao(database);
			case FLOW_PROPERTY -> new FlowPropertyDao(database);
			case IMPACT_METHOD -> new ImpactMethodDao(database);
			case IMPACT_CATEGORY -> new ImpactCategoryDao(database);
			case LOCATION -> new LocationDao(database);
			case PARAMETER -> new ParameterDao(database);
			case PROCESS -> new ProcessDao(database);
			case PRODUCT_SYSTEM -> new ProductSystemDao(database);
			case PROJECT -> new ProjectDao(database);
			case SOCIAL_INDICATOR -> new SocialIndicatorDao(database);
			case SOURCE -> new SourceDao(database);
			case UNIT_GROUP -> new UnitGroupDao(database);
			default -> null;
		};
	}
}
