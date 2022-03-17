package org.openlca.jsonld.input;

import org.openlca.core.database.ActorDao;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.EpdDao;
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
import org.openlca.core.database.ResultDao;
import org.openlca.core.database.RefEntityDao;
import org.openlca.core.database.SocialIndicatorDao;
import org.openlca.core.database.SourceDao;
import org.openlca.core.database.UnitDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Epd;
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
import org.openlca.core.model.Result;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

import java.util.HashMap;
import java.util.Map;

class Db {

	private final Map<String, Long> categoryIds = new HashMap<>();
	private final Map<String, Long> unitGroupIds = new HashMap<>();
	private final Map<String, Long> unitIds = new HashMap<>();
	private final Map<String, Long> flowPropertyIds = new HashMap<>();
	private final Map<String, Long> flowIds = new HashMap<>();
	private final Map<String, Long> locationIds = new HashMap<>();
	private final Map<String, Long> dqSystemIds = new HashMap<>();
	private final Map<String, Long> methodIds = new HashMap<>();
	private final Map<String, Long> impactCategoryIds = new HashMap<>();
	private final Map<String, Long> nwSetIds = new HashMap<>();
	private final Map<String, Long> actorIds = new HashMap<>();
	private final Map<String, Long> sourceIds = new HashMap<>();
	private final Map<String, Long> parameterIds = new HashMap<>();
	private final Map<String, Long> processIds = new HashMap<>();
	private final Map<String, Long> indicatorIds = new HashMap<>();
	private final Map<String, Long> currencyIds = new HashMap<>();
	private final Map<String, Long> systemIds = new HashMap<>();
	private final Map<String, Long> projectIds = new HashMap<>();
	private final Map<String, Long> resultIds = new HashMap<>();
	private final Map<String, Long> epdIds = new HashMap<>();
	public Map<String, String> categoryRefIdMapping = new HashMap<>();

	private final IDatabase db;

	public Db(IDatabase db) {
		this.db = db;
	}

	public IDatabase getDatabase() {
		return db;
	}

	@SuppressWarnings("unchecked")
	<T extends RefEntity> T get(ModelType modelType, String refId) {
		return switch (modelType) {
			case PROJECT -> (T) get(new ProjectDao(db), refId, projectIds);
			case PRODUCT_SYSTEM -> (T) get(new ProductSystemDao(db), refId, systemIds);
			case PROCESS -> (T) get(new ProcessDao(db), refId, processIds);
			case FLOW -> (T) get(new FlowDao(db), refId, flowIds);
			case IMPACT_METHOD -> (T) get(new ImpactMethodDao(db), refId, methodIds);
			case IMPACT_CATEGORY -> (T) get(new ImpactCategoryDao(db), refId, impactCategoryIds);
			case NW_SET -> (T) get(new NwSetDao(db), refId, nwSetIds);
			case SOCIAL_INDICATOR -> (T) get(new SocialIndicatorDao(db), refId, indicatorIds);
			case PARAMETER -> (T) get(new ParameterDao(db), refId, parameterIds);
			case DQ_SYSTEM -> (T) get(new DQSystemDao(db), refId, dqSystemIds);
			case FLOW_PROPERTY -> (T) get(new FlowPropertyDao(db), refId, flowPropertyIds);
			case UNIT_GROUP -> (T) get(new UnitGroupDao(db), refId, unitGroupIds);
			case UNIT -> (T) get(new UnitDao(db), refId, unitIds);
			case CURRENCY -> (T) get(new CurrencyDao(db), refId, currencyIds);
			case ACTOR -> (T) get(new ActorDao(db), refId, actorIds);
			case SOURCE -> (T) get(new SourceDao(db), refId, sourceIds);
			case LOCATION -> (T) get(new LocationDao(db), refId, locationIds);
			case CATEGORY -> (T) get(new CategoryDao(db), refId, categoryIds);
			case RESULT -> (T) get(new ResultDao(db), refId, resultIds);
			case EPD -> (T) get(new EpdDao(db), refId, epdIds);
			default -> throw new RuntimeException(modelType.name() + " not supported");
		};
	}

	@SuppressWarnings("unchecked")
	<T extends RefEntity> T put(T entity) {
		if (entity == null)
			return null;
		var modelType = ModelType.forModelClass(entity.getClass());
		if (modelType == null)
			throw new RuntimeException(entity.getClass().getCanonicalName() + " not supported");
		return switch (modelType) {
			case PROJECT -> (T) put(new ProjectDao(db), (Project) entity, projectIds);
			case PRODUCT_SYSTEM -> (T) put(new ProductSystemDao(db), (ProductSystem) entity, systemIds);
			case PROCESS -> (T) put(new ProcessDao(db), (Process) entity, processIds);
			case FLOW -> (T) put(new FlowDao(db), (Flow) entity, flowIds);
			case IMPACT_CATEGORY -> (T) put(new ImpactCategoryDao(db), (ImpactCategory) entity, impactCategoryIds);
			case IMPACT_METHOD -> (T) put(new ImpactMethodDao(db), (ImpactMethod) entity, methodIds);
			case SOCIAL_INDICATOR -> (T) put(new SocialIndicatorDao(db), (SocialIndicator) entity, indicatorIds);
			case PARAMETER -> (T) put(new ParameterDao(db), (Parameter) entity, parameterIds);
			case DQ_SYSTEM -> (T) put(new DQSystemDao(db), (DQSystem) entity, dqSystemIds);
			case FLOW_PROPERTY -> (T) put(new FlowPropertyDao(db), (FlowProperty) entity, flowPropertyIds);
			case UNIT_GROUP -> (T) put((UnitGroup) entity);
			case CURRENCY -> (T) put(new CurrencyDao(db), (Currency) entity, currencyIds);
			case ACTOR -> (T) put(new ActorDao(db), (Actor) entity, actorIds);
			case SOURCE -> (T) put(new SourceDao(db), (Source) entity, sourceIds);
			case LOCATION -> (T) put(new LocationDao(db), (Location) entity, locationIds);
			case CATEGORY -> (T) put(new CategoryDao(db), (Category) entity, categoryIds);
			case RESULT -> (T) put(new ResultDao(db), (Result) entity, resultIds);
			case EPD -> (T) put(new EpdDao(db), (Epd) entity, epdIds);
			default -> throw new RuntimeException(modelType.name() + " not supported");
		};
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

	private <T extends RefEntity> T get(
            RefEntityDao<T, ?> dao, String refId, Map<String, Long> idCache) {
		Long id = idCache.get(refId);
		if (id != null)
			return dao.getForId(id);
		T entity = dao.getForRefId(refId);
		if (entity == null)
			return null;
		idCache.put(refId, entity.id);
		return entity;
	}

	private <T extends RefEntity> T put(
            RefEntityDao<T, ?> dao, T entity, Map<String, Long> idCache) {
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
