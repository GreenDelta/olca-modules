package org.openlca.io.xls.process.input;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.BaseDescriptor;

import java.util.HashMap;

class RefData {

	private HashMap<String, Unit> units = new HashMap<>();
	private HashMap<String, UnitGroup> unitGroups = new HashMap<>();
	private HashMap<String, FlowProperty> flowProperties = new HashMap<>();

	/**
	 * Loads the units, unit groups and flow properties from the database.
	 * This method must be called after unit sheets where read.
	 */
	void loadUnits(IDatabase database) throws Exception {
		RootEntityDao<Unit, BaseDescriptor> unitDao = new RootEntityDao<>(
				Unit.class, BaseDescriptor.class, database);
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

}
