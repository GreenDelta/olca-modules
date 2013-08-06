package org.openlca.core.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.ActorDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.openlca.core.model.descriptors.ProjectDescriptor;
import org.openlca.core.model.descriptors.SourceDescriptor;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;

public class Cache {

	private final Map<String, Object> cache = new HashMap<>();
	private final ActorDao actorDao;
	private final SourceDao sourceDao;
	private final UnitGroupDao unitGroupDao;
	private final FlowPropertyDao flowPropertyDao;
	private final FlowDao flowDao;
	private final ProcessDao processDao;
	private final ProductSystemDao productSystemDao;
	private final ImpactMethodDao impactMethodDao;
	private final ProjectDao projectDao;
	private final LocationDao locationDao;
	private final CategoryDao categoryDao;

	public Cache(IDatabase database) {
		actorDao = new ActorDao(database);
		sourceDao = new SourceDao(database);
		unitGroupDao = new UnitGroupDao(database);
		flowPropertyDao = new FlowPropertyDao(database);
		flowDao = new FlowDao(database);
		processDao = new ProcessDao(database);
		productSystemDao = new ProductSystemDao(database);
		impactMethodDao = new ImpactMethodDao(database);
		projectDao = new ProjectDao(database);
		locationDao = new LocationDao(database);
		categoryDao = new CategoryDao(database);
	}

	public void load() {
		for (Category category : categoryDao.getAll())
			cache.put(getKey(Category.class, category.getId()), category);
		for (UnitGroup unitGroup : unitGroupDao.getAll())
			cache.put(getKey(UnitGroup.class, unitGroup.getId()), unitGroup);
		for (FlowProperty flowProperty : flowPropertyDao.getAll())
			cache.put(getKey(FlowProperty.class, flowProperty.getId()),
					flowProperty);
		for (Location location : locationDao.getAll())
			cache.put(getKey(Location.class, location.getId()), location);
	}

	public ActorDescriptor getActorDescriptor(long id) {
		return getDescriptor(actorDao, id);
	}

	public List<ActorDescriptor> getActorDescriptors(Set<Long> ids) {
		return getDescriptors(actorDao, ids);
	}

	public SourceDescriptor getSourceDescriptor(long id) {
		return getDescriptor(sourceDao, id);
	}

	public List<SourceDescriptor> getSourceDescriptors(Set<Long> ids) {
		return getDescriptors(sourceDao, ids);
	}

	public UnitGroupDescriptor getUnitGroupDescriptor(long id) {
		return getDescriptor(unitGroupDao, id);
	}

	public List<UnitGroupDescriptor> getUnitGroupDescriptors(Set<Long> ids) {
		return getDescriptors(unitGroupDao, ids);
	}

	public FlowPropertyDescriptor getFlowPropertyDescriptor(long id) {
		return getDescriptor(flowPropertyDao, id);
	}

	public List<FlowPropertyDescriptor> getFlowPropertyDescriptors(Set<Long> ids) {
		return getDescriptors(flowPropertyDao, ids);
	}

	public FlowDescriptor getFlowDescriptor(long id) {
		return getDescriptor(flowDao, id);
	}

	public List<FlowDescriptor> getFlowDescriptors(Set<Long> ids) {
		return getDescriptors(flowDao, ids);
	}

	public ProcessDescriptor getProcessDescriptor(long id) {
		return getDescriptor(processDao, id);
	}

	public List<ProcessDescriptor> getProcessDescriptors(Set<Long> ids) {
		return getDescriptors(processDao, ids);
	}

	public ProductSystemDescriptor getProductSystemDescriptor(long id) {
		return getDescriptor(productSystemDao, id);
	}

	public List<ProductSystemDescriptor> getProductSystemDescriptors(
			Set<Long> ids) {
		return getDescriptors(productSystemDao, ids);
	}

	public ImpactMethodDescriptor getImpactMethodDescriptor(long id) {
		return getDescriptor(impactMethodDao, id);
	}

	public List<ImpactMethodDescriptor> getImpactMethodDescriptors(Set<Long> ids) {
		return getDescriptors(impactMethodDao, ids);
	}

	public ProjectDescriptor getProjectDescriptor(long id) {
		return getDescriptor(projectDao, id);
	}

	public List<ProjectDescriptor> getProjectDescriptors(Set<Long> ids) {
		return getDescriptors(projectDao, ids);
	}

	public Location getLocation(long id) {
		return get(locationDao, id);
	}

	public List<Location> getLocations(Set<Long> ids) {
		return get(locationDao, ids);
	}

	public Category getCategory(long id) {
		return get(categoryDao, id);
	}

	public List<Category> getCategorys(Set<Long> ids) {
		return get(categoryDao, ids);
	}

	public UnitGroup getUnitGroup(long id) {
		return get(unitGroupDao, id);
	}

	public List<UnitGroup> getUnitGroups(Set<Long> ids) {
		return get(unitGroupDao, ids);
	}

	public FlowProperty getFlowProperty(long id) {
		return get(flowPropertyDao, id);
	}

	public List<FlowProperty> getFlowProperties(Set<Long> ids) {
		return get(flowPropertyDao, ids);
	}

	private <T extends CategorizedEntity, V extends CategorizedDescriptor> V getDescriptor(
			CategorizedEntityDao<T, V> dao, long id) {
		String key = getKey(dao.getDescriptorType(), id);
		@SuppressWarnings("unchecked")
		V value = (V) cache.get(key);
		if (value == null) {
			value = dao.getDescriptor(id);
			cache.put(key, value);
		}
		return value;
	}

	private <T extends CategorizedEntity, V extends CategorizedDescriptor> List<V> getDescriptors(
			CategorizedEntityDao<T, V> dao, Set<Long> ids) {
		List<V> results = new ArrayList<>();
		Set<Long> toLoad = new HashSet<>();
		for (Long id : ids) {
			String key = getKey(dao.getDescriptorType(), id);
			@SuppressWarnings("unchecked")
			V value = (V) cache.get(key);
			if (value == null)
				toLoad.add(id);
			else
				results.add(value);
		}
		List<V> rest = dao.getDescriptors(toLoad);
		for (V v : rest) {
			cache.put(getKey(dao.getDescriptorType(), v.getId()), v);
			results.add(v);
		}
		return results;
	}

	private <T extends RootEntity> T get(BaseDao<T> dao, long id) {
		String key = getKey(dao.getEntityType(), id);
		@SuppressWarnings("unchecked")
		T value = (T) cache.get(key);
		if (value == null) {
			value = dao.getForId(id);
			cache.put(key, value);
		}
		return value;
	}

	private <T extends RootEntity> List<T> get(BaseDao<T> dao, Set<Long> ids) {
		List<T> results = new ArrayList<>();
		Set<Long> toLoad = new HashSet<>();
		for (Long id : ids) {
			String key = getKey(dao.getEntityType(), id);
			@SuppressWarnings("unchecked")
			T value = (T) cache.get(key);
			if (value == null)
				toLoad.add(id);
			else
				results.add(value);
		}
		List<T> rest = dao.getForIds(toLoad);
		for (T t : rest) {
			cache.put(getKey(dao.getEntityType(), t.getId()), t);
			results.add(t);
		}
		return results;
	}

	private String getKey(Class<?> clazz, long id) {
		return clazz.getSimpleName() + "_" + id;
	}

}
