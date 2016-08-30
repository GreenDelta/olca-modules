package org.openlca.jsonld.input;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.ActorDao;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.database.SocialIndicatorDao;
import org.openlca.core.database.SourceDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.BaseDescriptor;

class Db {

	private Map<String, Long> categoryIds = new HashMap<>();
	private Map<String, Long> unitGroupIds = new HashMap<>();
	private Map<String, Long> unitIds = new HashMap<>();
	private Map<String, Long> flowPropertyIds = new HashMap<>();
	private Map<String, Long> flowIds = new HashMap<>();
	private Map<String, Long> locationIds = new HashMap<>();
	private Map<String, Long> dqSystemIds = new HashMap<>();
	private Map<String, Long> methodIds = new HashMap<>();
	private Map<String, Long> actorIds = new HashMap<>();
	private Map<String, Long> sourceIds = new HashMap<>();
	private Map<String, Long> parameterIds = new HashMap<>();
	private Map<String, Long> processIds = new HashMap<>();
	private Map<String, Long> indicatorIds = new HashMap<>();
	private Map<String, Long> currencyIds = new HashMap<>();
	private Map<String, Long> systemIds = new HashMap<>();
	private Map<String, Long> projectIds = new HashMap<>();
	public Map<String, String> categoryRefIdMapping = new HashMap<String, String>();
	
	private IDatabase db;

	public Db(IDatabase db) {
		this.db = db;
	}

	public IDatabase getDatabase() {
		return db;
	}

	public Location getLocation(String refId) {
		return get(new LocationDao(db), refId, locationIds);
	}

	public Location put(Location loc) {
		return put(new LocationDao(db), loc, locationIds);
	}

	public DQSystem getDqSystem(String refId) {
		return get(new DQSystemDao(db), refId, dqSystemIds);
	}

	public DQSystem put(DQSystem sys) {
		return put(new DQSystemDao(db), sys, dqSystemIds);
	}

	public Category getCategory(String refId) {
		return get(new CategoryDao(db), refId, categoryIds);
	}

	public Category put(Category category) {
		return put(new CategoryDao(db), category, categoryIds);
	}

	public Category updateChilds(Category category) {
		if (category == null)
			return null;
		CategoryDao dao = new CategoryDao(db);
		Category cat = dao.update(category);
		for (Category child : cat.getChildCategories()) {
			String refId = child.getRefId();
			if (categoryIds.containsKey(refId))
				continue;
			categoryIds.put(refId, child.getId());
		}
		return cat;
	}

	public Actor getActor(String refId) {
		return get(new ActorDao(db), refId, actorIds);
	}

	public Actor put(Actor actor) {
		return put(new ActorDao(db), actor, actorIds);
	}

	public Source getSource(String refId) {
		return get(new SourceDao(db), refId, sourceIds);
	}

	public Source put(Source source) {
		return put(new SourceDao(db), source, sourceIds);
	}

	public UnitGroup getUnitGroup(String refId) {
		return get(new UnitGroupDao(db), refId, unitGroupIds);
	}

	public UnitGroup put(UnitGroup unitGroup) {
		UnitGroup g = put(new UnitGroupDao(db), unitGroup, unitGroupIds);
		if (g == null)
			return null;
		for (Unit unit : g.getUnits())
			unitIds.put(unit.getRefId(), unit.getId());
		return g;
	}

	public UnitGroup update(UnitGroup group) {
		return new UnitGroupDao(db).update(group);
	}

	public Unit getUnit(String refId) {
		RootEntityDao<Unit, BaseDescriptor> dao = new RootEntityDao<>(
				Unit.class, BaseDescriptor.class, db);
		return get(dao, refId, unitIds);
	}

	public FlowProperty getFlowProperty(String refId) {
		return get(new FlowPropertyDao(db), refId, flowPropertyIds);
	}

	public FlowProperty put(FlowProperty property) {
		return put(new FlowPropertyDao(db), property, flowPropertyIds);
	}

	public Flow getFlow(String refId) {
		return get(new FlowDao(db), refId, flowIds);
	}

	public Flow put(Flow flow) {
		return put(new FlowDao(db), flow, flowIds);
	}

	public ImpactMethod getMethod(String refId) {
		return get(new ImpactMethodDao(db), refId, methodIds);
	}

	public ImpactMethod put(ImpactMethod method) {
		return put(new ImpactMethodDao(db), method, methodIds);
	}

	public Process getProcess(String refId) {
		return get(new ProcessDao(db), refId, processIds);
	}

	public Process put(Process process) {
		return put(new ProcessDao(db), process, processIds);
	}

	public SocialIndicator getSocialIndicator(String refId) {
		return get(new SocialIndicatorDao(db), refId, indicatorIds);
	}

	public SocialIndicator put(SocialIndicator indicator) {
		return put(new SocialIndicatorDao(db), indicator, indicatorIds);
	}

	public Currency getCurrency(String refId) {
		return get(new CurrencyDao(db), refId, currencyIds);
	}

	public Currency put(Currency currency) {
		return put(new CurrencyDao(db), currency, currencyIds);
	}

	public Parameter getParameter(String refId) {
		return get(new ParameterDao(db), refId, parameterIds);
	}

	public Parameter put(Parameter parameter) {
		return put(new ParameterDao(db), parameter, parameterIds);
	}

	public ProductSystem getSystem(String refId) {
		return get(new ProductSystemDao(db), refId, systemIds);
	}

	public ProductSystem put(ProductSystem system) {
		return put(new ProductSystemDao(db), system, systemIds);
	}

	public Project getProject(String refId) {
		return get(new ProjectDao(db), refId, projectIds);
	}

	public Project put(Project project) {
		return put(new ProjectDao(db), project, projectIds);
	}

	private <T extends RootEntity> T get(RootEntityDao<T, ?> dao, String refId,
			Map<String, Long> idCache) {
		Long id = idCache.get(refId);
		if (id != null)
			return dao.getForId(id);
		T entity = dao.getForRefId(refId);
		if (entity == null)
			return null;
		idCache.put(refId, entity.getId());
		return entity;
	}

	private <T extends RootEntity> T put(RootEntityDao<T, ?> dao, T entity,
			Map<String, Long> idCache) {
		if (entity == null)
			return null;
		if (entity.getId() == 0L)
			entity = dao.insert(entity);
		else {
			dao.detach(dao.getForId(entity.getId()));
			entity = dao.update(entity);
		}
		idCache.put(entity.getRefId(), entity.getId());
		return entity;
	}
}
