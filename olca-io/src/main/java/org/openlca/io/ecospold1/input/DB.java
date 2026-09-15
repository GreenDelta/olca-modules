package org.openlca.io.ecospold1.input;

import java.util.HashMap;
import java.util.Map;

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
import org.openlca.ecospold.model.DataSet;
import org.openlca.ecospold.model.IExchange;
import org.openlca.ecospold.model.IPerson;
import org.openlca.ecospold.model.ISource;
import org.openlca.io.UnitMappingEntry;
import org.openlca.util.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper for the database access and (semantic) search of entities for the
 * import of EcoSpold data sets.
 */
class DB {

	final IDatabase db;
	private final DBSearch search;
	private final Logger log = LoggerFactory.getLogger(getClass());

	/// Cache for resolved categories. The values are detached snapshots that are
	/// only used to assign a category to an entity, which should be fine even if
	/// other parts of the same category tree are updated.
	private final Map<String, Category> categories = new HashMap<>();

	private final Map<String, Actor> actors = new HashMap<>();
	private final Map<String, Source> sources = new HashMap<>();
	private final Map<String, Location> locations = new HashMap<>();
	private final Map<String, Flow> flows = new HashMap<>();

	public DB(IDatabase db) {
		this.db = db;
		this.search = new DBSearch(db);
	}

	public Category resolveCategory(ModelType type, String top, String sub) {
		var path = Util.categoryPathOf(top, sub);
		if (type == null || path.length == 0)
			return null;
		var key = categoryKey(type, path);
		var resolved = categories.get(key);
		if (resolved != null)
			return resolved;
		resolved = CategoryDao.sync(db, type, path);
		if (resolved != null) {
			categories.put(key, resolved);
		}
		return resolved;
	}

	private static String categoryKey(ModelType type, String[] path) {
		var names = new String[path.length + 1];
		names[0] = type.name();
		System.arraycopy(path, 0, names, 1, path.length);
		return KeyGen.toPath(names);
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

	private <T extends RootEntity> T get(
		Class<T> type, Map<String, T> cache, String genKey) {
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
			var modelType = ModelType.of(type);
			return (T) Daos.root(db, modelType).getForRefId(id);
		} catch (Exception e) {
			log.error("Failed to query database for {} id={}", type, id, e);
			return null;
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T extends AbstractEntity> void put(T entity, String genKey) {
		if (entity == null)
			return;
		try {
			Class<T> clazz = (Class<T>) entity.getClass();
			Daos.base(db, clazz).insert(entity);
			Map cache = getCache(entity);
			if (cache != null)
				cache.put(genKey, entity);
		} catch (Exception e) {
			log.error("Failed to save entity {} id={}", entity, genKey, e);
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
