package org.openlca.io.ecospold1.importer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IPerson;
import org.openlca.ecospold.ISource;
import org.openlca.ecospold.io.DataSet;
import org.openlca.io.UnitMappingEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper for the database access and (semantic) search of entities for the
 * import of EcoSpold data sets.
 */
class DB {

	private IDatabase database;
	private DBSearch search;
	private Logger log = LoggerFactory.getLogger(getClass());

	private Map<String, Category> categories = new HashMap<>();
	private Map<String, Actor> actors = new HashMap<>();
	private Map<String, Source> sources = new HashMap<>();
	private Map<String, Location> locations = new HashMap<>();
	private Map<String, Flow> flows = new HashMap<>();

	public DB(IDatabase database) {
		this.database = database;
		this.search = new DBSearch(database);
	}

	public Category getPutCategory(ModelType type, String parentName,
			String name) {
		String key = StringUtils.join(new Object[] { type.name(), parentName,
				name }, "/");
		Category category = categories.get(key);
		if (category != null)
			return category;
		try {
			CategoryDao dao = new CategoryDao(database.getEntityFactory());
			dao.getRootCategories(type);
			Category parent = null;
			if (parentName != null) {
				parent = findRoot(parentName, type);
				if (parent == null)
					parent = createRoot(type, parentName);
			}
			category = parent;
			if (name != null) {
				if (parent != null)
					category = findChildOrAdd(parent, name);
				else
					category = createRoot(type, name);
			}
			categories.put(key, category);
			return category;
		} catch (Exception e) {
			log.error("Failed to get category " + key, e);
			return null;
		}
	}

	private Category createRoot(ModelType type, String name) {
		try {
			Category category = new Category();
			category.setRefId(UUID.randomUUID().toString());
			category.setModelType(type);
			category.setName(name);
			database.createDao(Category.class).insert(category);
			return category;
		} catch (Exception e) {
			log.error("failed to insert root category " + type + " name", e);
			return null;
		}
	}

	private Category findRoot(String name, ModelType type) {
		try {
			CategoryDao dao = new CategoryDao(database.getEntityFactory());
			for (Category root : dao.getRootCategories(type)) {
				if (StringUtils.equalsIgnoreCase(root.getName(), name))
					return root;
			}
			return null;
		} catch (Exception e) {
			log.error("failed to search root category " + name + " " + type, e);
			return null;
		}
	}

	public Category getPutCategory(Category root, String parentName, String name) {
		String key = StringUtils.join(new Object[] { root.getName(),
				parentName, name }, "/");
		Category category = categories.get(key);
		if (category != null)
			return category;
		try {
			Category parent = root;
			if (parentName != null)
				parent = findChildOrAdd(root, parentName);
			Category cat = parent;
			if (name != null)
				cat = findChildOrAdd(parent, name);
			return cacheReturn(key, cat);
		} catch (Exception e) {
			log.error("Failed to find or add category", e);
			return null;
		}
	}

	private Category cacheReturn(String key, Category category) {
		categories.put(key, category);
		return category;
	}

	private Category findChildOrAdd(Category root, String childName)
			throws Exception {
		for (Category child : root.getChildCategories()) {
			if (StringUtils.equalsIgnoreCase(child.getName(), childName))
				return child;
		}
		Category child = new Category(UUID.randomUUID().toString(), childName,
				root.getModelType());
		child.setParentCategory(root);
		root.add(child);
		database.createDao(Category.class).update(root);
		return child;
	}

	public Actor findActor(IPerson person, String genKey) {
		Actor actor = get(Actor.class, actors, genKey);
		if (actor != null)
			return actor;
		actor = search.findActor(person);
		if (actor != null)
			actors.put(genKey, actor);
		return actor;
	}

	public Source findSource(ISource eSource, String genKey) {
		Source source = get(Source.class, sources, genKey);
		if (source != null)
			return source;
		source = search.findSource(eSource);
		if (source != null)
			sources.put(genKey, source);
		return source;
	}

	public Location findLocation(String locationCode, String genKey) {
		Location location = get(Location.class, locations, genKey);
		if (location != null)
			return location;
		location = search.findLocation(locationCode);
		if (location != null)
			locations.put(genKey, location);
		return location;
	}

	public Flow findFlow(IExchange exchange, String genKey,
			UnitMappingEntry unitMapping) {
		Flow flow = get(Flow.class, flows, genKey);
		if (flow != null)
			return flow;
		flow = search.findFlow(exchange, unitMapping);
		if (flow != null)
			flows.put(genKey, flow);
		return flow;
	}

	public Flow findFlow(DataSet dataSet, String genKey,
			UnitMappingEntry unitMapping) {
		Flow flow = get(Flow.class, flows, genKey);
		if (flow != null)
			return flow;
		flow = search.findFlow(dataSet, unitMapping);
		if (flow != null)
			flows.put(genKey, flow);
		return flow;
	}

	private <T> T get(Class<T> type, Map<String, T> cache, String genKey) {
		T entity = cache.get(genKey);
		if (entity != null)
			return entity;
		entity = get(type, genKey);
		if (entity != null)
			cache.put(genKey, entity);
		return entity;
	}

	public <T> T get(Class<T> type, String id) {
		try {
			return database.createDao(type).getForId(id);
		} catch (Exception e) {
			log.error("Failed to query database for " + type + " id=" + id, e);
			return null;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> void put(T entity, String genKey) {
		if (entity == null)
			return;
		try {
			Class<T> clazz = (Class<T>) entity.getClass();
			database.createDao(clazz).insert(entity);
			Map cache = getCache(entity);
			if (cache != null)
				cache.put(genKey, entity);
		} catch (Exception e) {
			log.error("Failed to save entity " + entity + " id=" + genKey, e);
		}
	}

	private Map<String, ?> getCache(Object entity) {
		if (entity instanceof Actor)
			return actors;
		if (entity instanceof Source)
			return sources;
		if (entity instanceof Location)
			return locations;
		if (entity instanceof Flow)
			return flows;
		return null;
	}
}