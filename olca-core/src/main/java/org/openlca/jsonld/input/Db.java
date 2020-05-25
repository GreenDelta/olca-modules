package org.openlca.jsonld.input;

import org.openlca.core.database.ActorDao;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.NwSetDao;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.database.SocialIndicatorDao;
import org.openlca.core.database.SourceDao;
import org.openlca.core.database.UnitDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

import java.util.HashMap;
import java.util.Map;

class Db {

	private Map<String, Long> categoryIds = new HashMap<>();
	private Map<String, Long> unitGroupIds = new HashMap<>();
	private Map<String, Long> unitIds = new HashMap<>();
	private Map<String, Long> flowPropertyIds = new HashMap<>();
	private Map<String, Long> flowIds = new HashMap<>();
	private Map<String, Long> locationIds = new HashMap<>();
	private Map<String, Long> dqSystemIds = new HashMap<>();
	private Map<String, Long> methodIds = new HashMap<>();
	private Map<String, Long> impactCategoryIds = new HashMap<>();
	private Map<String, Long> nwSetIds = new HashMap<>();
	private Map<String, Long> actorIds = new HashMap<>();
	private Map<String, Long> sourceIds = new HashMap<>();
	private Map<String, Long> parameterIds = new HashMap<>();
	private Map<String, Long> processIds = new HashMap<>();
	private Map<String, Long> indicatorIds = new HashMap<>();
	private Map<String, Long> currencyIds = new HashMap<>();
	private Map<String, Long> systemIds = new HashMap<>();
	private Map<String, Long> projectIds = new HashMap<>();
	public Map<String, String> categoryRefIdMapping = new HashMap<>();

	private IDatabase db;

	public Db(IDatabase db) {
		this.db = db;
	}

	public IDatabase getDatabase() {
		return db;
	}

	@SuppressWarnings("unchecked")
	<T extends RootEntity> T get(ModelType modelType, String refId) {
		switch (modelType) {
		case PROJECT:
			return (T) get(new ProjectDao(db), refId, projectIds);
		case PRODUCT_SYSTEM:
			return (T) get(new ProductSystemDao(db), refId, systemIds);
		case PROCESS:
			return (T) get(new ProcessDao(db), refId, processIds);
		case FLOW:
			return (T) get(new FlowDao(db), refId, flowIds);
		case IMPACT_METHOD:
			return (T) get(new ImpactMethodDao(db), refId, methodIds);
		case IMPACT_CATEGORY:
			return (T) get(new ImpactCategoryDao(db), refId, impactCategoryIds);
		case NW_SET:
			return (T) get(new NwSetDao(db), refId, nwSetIds);
		case SOCIAL_INDICATOR:
			return (T) get(new SocialIndicatorDao(db), refId, indicatorIds);
		case PARAMETER:
			return (T) get(new ParameterDao(db), refId, parameterIds);
		case DQ_SYSTEM:
			return (T) get(new DQSystemDao(db), refId, dqSystemIds);
		case FLOW_PROPERTY:
			return (T) get(new FlowPropertyDao(db), refId, flowPropertyIds);
		case UNIT_GROUP:
			return (T) get(new UnitGroupDao(db), refId, unitGroupIds);
		case UNIT:
			return (T) get(new UnitDao(db), refId, unitIds);
		case CURRENCY:
			return (T) get(new CurrencyDao(db), refId, currencyIds);
		case ACTOR:
			return (T) get(new ActorDao(db), refId, actorIds);
		case SOURCE:
			return (T) get(new SourceDao(db), refId, sourceIds);
		case LOCATION:
			return (T) get(new LocationDao(db), refId, locationIds);
		case CATEGORY:
			return (T) get(new CategoryDao(db), refId, categoryIds);
		default:
			throw new RuntimeException(modelType.name() + " not supported");
		}
	}

	@SuppressWarnings("unchecked")
	<T extends RootEntity> T put(T entity) {
		if (entity == null)
			return null;
		ModelType modelType = ModelType.forModelClass(entity.getClass());
		if (modelType == null)
			throw new RuntimeException(entity.getClass().getCanonicalName() + " not supported");
		switch (modelType) {
		case PROJECT:
			return (T) put(new ProjectDao(db), (Project) entity, projectIds);
		case PRODUCT_SYSTEM:
			return (T) put(new ProductSystemDao(db), (ProductSystem) entity, systemIds);
		case PROCESS:
			return (T) put(new ProcessDao(db), (Process) entity, processIds);
		case FLOW:
			return (T) put(new FlowDao(db), (Flow) entity, flowIds);
		case IMPACT_CATEGORY:
			return (T) put(new ImpactCategoryDao(db), (ImpactCategory) entity, impactCategoryIds);
		case IMPACT_METHOD:
			return (T) put(new ImpactMethodDao(db), (ImpactMethod) entity, methodIds);
		case SOCIAL_INDICATOR:
			return (T) put(new SocialIndicatorDao(db), (SocialIndicator) entity, indicatorIds);
		case PARAMETER:
			return (T) put(new ParameterDao(db), (Parameter) entity, parameterIds);
		case DQ_SYSTEM:
			return (T) put(new DQSystemDao(db), (DQSystem) entity, dqSystemIds);
		case FLOW_PROPERTY:
			return (T) put(new FlowPropertyDao(db), (FlowProperty) entity, flowPropertyIds);
		case UNIT_GROUP:
			return (T) put((UnitGroup) entity);
		case CURRENCY:
			return (T) put(new CurrencyDao(db), (Currency) entity, currencyIds);
		case ACTOR:
			return (T) put(new ActorDao(db), (Actor) entity, actorIds);
		case SOURCE:
			return (T) put(new SourceDao(db), (Source) entity, sourceIds);
		case LOCATION:
			return (T) put(new LocationDao(db), (Location) entity, locationIds);
		case CATEGORY:
			return (T) put(new CategoryDao(db), (Category) entity, categoryIds);
		default:
			throw new RuntimeException(modelType.name() + " not supported");
		}
	}

	private UnitGroup put(UnitGroup unitGroup) {
		UnitGroup g = put(new UnitGroupDao(db), unitGroup, unitGroupIds);
		if (g == null)
			return null;
		for (Unit unit : g.units)
			unitIds.put(unit.refId, unit.id);
		return g;
	}

	public Category updateChilds(Category category) {
		if (category == null)
			return null;
		CategoryDao dao = new CategoryDao(db);
		Category cat = dao.update(category);
		for (Category child : cat.childCategories) {
			String refId = child.refId;
			if (categoryIds.containsKey(refId))
				continue;
			categoryIds.put(refId, child.id);
		}
		return cat;
	}

	public UnitGroup update(UnitGroup group) {
		return new UnitGroupDao(db).update(group);
	}

	private <T extends RootEntity> T get(RootEntityDao<T, ?> dao, String refId, Map<String, Long> idCache) {
		Long id = idCache.get(refId);
		if (id != null)
			return dao.getForId(id);
		T entity = dao.getForRefId(refId);
		if (entity == null)
			return null;
		idCache.put(refId, entity.id);
		return entity;
	}

	private <T extends RootEntity> T put(RootEntityDao<T, ?> dao, T entity, Map<String, Long> idCache) {
		if (entity == null)
			return null;
		if (entity.id == 0L)
			entity = dao.insert(entity);
		else {
			dao.detach(dao.getForId(entity.id));
			entity = dao.update(entity);
		}
		idCache.put(entity.refId, entity.id);
		return entity;
	}
}
