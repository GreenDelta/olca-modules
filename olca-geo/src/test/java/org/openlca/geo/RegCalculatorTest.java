package org.openlca.geo;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.UnitGroup;

import java.util.List;
import java.util.UUID;

public class RegCalculatorTest {

	private IDatabase db = Tests.getDb();

	private Flow flow(String name, String unit) {
		FlowDao dao = new FlowDao(db);
		List<Flow> flows = dao.getForName(name);
		if (!flows.isEmpty())
			return flows.get(0);
		Flow flow = new Flow();
		flow.name = name;
		flow.refId = UUID.randomUUID().toString();
		flow.addReferenceFactor(property(unit));
		return dao.insert(flow);
	}

	private FlowProperty property(String unit) {
		FlowPropertyDao dao = new FlowPropertyDao(db);
		List<FlowProperty> props = dao.getForName(unit);
		if (!props.isEmpty())
			return props.get(0);
		FlowProperty prop = new FlowProperty();
		prop.name = unit;
		prop.unitGroup = unitGroup(unit);
		return dao.insert(prop);
	}

	private UnitGroup unitGroup(String unit) {
		UnitGroupDao dao = new UnitGroupDao(db);
		List<UnitGroup> groups = dao.getForName(unit);
		if (!groups.isEmpty())
			return groups.get(0);
		UnitGroup group = new UnitGroup();
		group.name = unit;
		group.addReferenceUnit(unit);
		return dao.insert(group);
	}

	private Location location(String code) {
		LocationDao dao = new LocationDao(db);
		Location loc = dao.getForRefId(code);
		if (loc != null)
			return loc;
		loc = new Location();
		loc.refId = code;
		loc.code = code;
		loc.name = code;
		return dao.insert(loc);
	}

}
