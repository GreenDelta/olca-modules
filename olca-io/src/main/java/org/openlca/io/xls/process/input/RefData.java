package org.openlca.io.xls.process.input;

import java.util.HashMap;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.database.UnitDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

class RefData {

	private final HashMap<String, Actor> actors = new HashMap<>();
	private final HashMap<String, FlowProperty> flowProperties = new HashMap<>();
	private final HashMap<String, Flow> flows = new HashMap<>();
	private final HashMap<String, Location> locations = new HashMap<>();
	private final HashMap<String, Source> sources = new HashMap<>();
	private final HashMap<String, UnitGroup> unitGroups = new HashMap<>();
	private final HashMap<String, Unit> units = new HashMap<>();

	/**
	 * Loads the units, unit groups and flow properties from the database. This
	 * method must be called after unit sheets where read.
	 */
	void loadUnits(IDatabase database) throws Exception {
		UnitDao unitDao = new UnitDao(database);
		load(unitDao, units);
		load(new UnitGroupDao(database), unitGroups);
		load(new FlowPropertyDao(database), flowProperties);
	}

	private <T extends RootEntity> void load(RootEntityDao<T, ?> dao,
			HashMap<String, T> map) throws Exception {
		for (T entity : dao.getAll()) {
			map.put(entity.getName(), entity);
		}
	}

	Unit getUnit(String name) {
		return units.get(name);
	}

	FlowProperty getFlowProperty(String name) {
		return flowProperties.get(name);
	}

	void putActor(String name, String category, Actor actor) {
		actors.put(key(name, category), actor);
	}

	Actor getActor(String name, String category) {
		return actors.get(key(name, category));
	}

	void putSource(String name, String category, Source source) {
		sources.put(key(name, category), source);
	}

	Source getSource(String name, String category) {
		return sources.get(key(name, category));
	}

	void putFlow(String name, String category, Flow flow) {
		flows.put(key(name, category), flow);
	}

	Flow getFlow(String name, String category) {
		return flows.get(key(name, category));
	}

	void putLocation(String code, Location location) {
		locations.put(key(code), location);
	}

	Location getLocation(String code) {
		return locations.get(key(code));
	}

	private String key(String name, String category) {
		if (name == null && category == null) {
			return null;
		}
		String key = category == null ? name : name + category;
		return key.trim().toLowerCase();
	}

	private String key(String name) {
		if (name == null) {
			return null;
		}
		return name.trim().toLowerCase();
	}

}
