package org.openlca.io.ecospold1.input;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Source;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IPerson;
import org.openlca.ecospold.ISource;
import org.openlca.ecospold.io.DataSet;
import org.openlca.io.Categories;
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
			CategoryDao dao = new CategoryDao(database);
			dao.getRootCategories(type);
			Category parent = null;
			if (parentName != null) {
				parent = Categories.findRoot(database, type, parentName);
				if (parent == null)
					parent = Categories.createRoot(database, type, parentName);
			}
			category = parent;
			if (name != null) {
				if (parent != null)
					category = Categories
							.findOrAddChild(database, parent, name);
				else
					category = Categories.createRoot(database, type, name);
			}
			categories.put(key, category);
			return category;
		} catch (Exception e) {
			log.error("Failed to get category " + key, e);
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
				parent = Categories.findOrAddChild(database, root, parentName);
			Category cat = parent;
			if (name != null)
				cat = Categories.findOrAddChild(database, parent, name);
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

	private <T extends RootEntity> T get(Class<T> type, Map<String, T> cache,
			String genKey) {
		T entity = cache.get(genKey);
		if (entity != null)
			return entity;
		entity = get(type, genKey);
		if (entity != null)
			cache.put(genKey, entity);
		return entity;
	}

	@SuppressWarnings("unchecked")
	public <T extends RootEntity> T get(Class<T> type, String id) {
		try {
			ModelType modelType = ModelType.forModelClass(type);
			return (T) Daos.root(database, modelType).getForRefId(id);
		} catch (Exception e) {
			log.error("Failed to query database for " + type + " id=" + id, e);
			return null;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T extends AbstractEntity> void put(T entity, String genKey) {
		if (entity == null)
			return;
		try {
			Class<T> clazz = (Class<T>) entity.getClass();
			Daos.base(database, clazz).insert(entity);
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