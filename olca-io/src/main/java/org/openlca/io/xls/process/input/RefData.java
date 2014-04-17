package org.openlca.io.xls.process.input;

import java.util.HashMap;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.BaseDescriptor;

class RefData {

	private final HashMap<String, Actor> actors = new HashMap<>();
	private final HashMap<String, FlowProperty> flowProperties = new HashMap<>();
	private final HashMap<String, Flow> flows = new HashMap<>();
	private final HashMap<String, Location> locations = new HashMap<>();
	private final HashMap<String, Source> sources = new HashMap<>();
	private final HashMap<String, UnitGroup> unitGroups = new HashMap<>();
	private final HashMap<String, Unit> units = new HashMap<>();

	Actor getActor(final String name, final String category) {
		return actors.get(key(name, category));
	}

	Flow getFlow(final String name, final String category) {
		return flows.get(key(name, category));
	}

	FlowProperty getFlowProperty(final String name) {
		return flowProperties.get(name);
	}

	Location getLocation(final String code) {
		return locations.get(key(code));
	}

	Source getSource(final String name, final String category) {
		return sources.get(key(name, category));
	}

	Unit getUnit(final String name) {
		return units.get(name);
	}

	private String key(final String name) {
		if (name == null) {
			return null;
		}
		return name.trim().toLowerCase();
	}

	private String key(final String name, final String category) {
		if (name == null && category == null) {
			return null;
		}
		final String key = category == null ? name : name + category;
		return key.trim().toLowerCase();
	}

	private <T extends RootEntity> void load(final RootEntityDao<T, ?> dao,
			final HashMap<String, T> map) throws Exception {
		for (final T entity : dao.getAll()) {
			map.put(entity.getName(), entity);
		}
	}

	/**
	 * Loads the units, unit groups and flow properties from the database. This
	 * method must be called after unit sheets where read.
	 */
	void loadUnits(final IDatabase database) throws Exception {
		final RootEntityDao<Unit, BaseDescriptor> unitDao = new RootEntityDao<>(
				Unit.class, BaseDescriptor.class, database);
		load(unitDao, units);
		load(new UnitGroupDao(database), unitGroups);
		load(new FlowPropertyDao(database), flowProperties);
	}

	void putActor(final String name, final String category, final Actor actor) {
		actors.put(key(name, category), actor);
	}

	void putFlow(final String name, final String category, final Flow flow) {
		flows.put(key(name, category), flow);
	}

	void putLocation(final String code, final Location location) {
		locations.put(key(code), location);
	}

	void putSource(final String name, final String category, final Source source) {
		sources.put(key(name, category), source);
	}

}
