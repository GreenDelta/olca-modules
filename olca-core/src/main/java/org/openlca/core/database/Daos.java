package org.openlca.core.database;

import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.MappingFile;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessGroupSet;
import org.openlca.core.model.ProjectVariant;
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
		ModelType type = ModelType.forModelClass(clazz);
		if (type != null)
			return (BaseDao<T>) root(database, type);
		if (clazz == Exchange.class) 
			return (BaseDao<T>) new ExchangeDao(database);
		if (clazz == MappingFile.class) 
			return (BaseDao<T>) new MappingFileDao(database);
		if (clazz == ProcessGroupSet.class) 
			return (BaseDao<T>) new ProcessGroupSetDao(database);
		if (clazz == ProjectVariant.class) 
			return (BaseDao<T>) new ProjectVariantDao(database);
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
		switch (type) {
		case ACTOR:
			return new ActorDao(database);
		case CATEGORY:
			return new CategoryDao(database);
		case CURRENCY:
			return new CurrencyDao(database);
		case DQ_SYSTEM:
			return new DQSystemDao(database);
		case FLOW:
			return new FlowDao(database);
		case FLOW_PROPERTY:
			return new FlowPropertyDao(database);
		case IMPACT_METHOD:
			return new ImpactMethodDao(database);
		case IMPACT_CATEGORY:
			return new ImpactCategoryDao(database);
		case LOCATION:
			return new LocationDao(database);
		case PARAMETER:
			return new ParameterDao(database);
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
		default:
			return null;
		}
	}
}
