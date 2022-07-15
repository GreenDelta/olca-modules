package org.openlca.jsonld.input;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.output.JsonExport;

public class UnitFlowPropLoopTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testLoop() {
		var units = db.insert(UnitGroup.of("Units of mass", "kg"));
		var mass = db.insert(FlowProperty.of("Mass", units));
		units.defaultFlowProperty = mass;
		units = db.update(units);

		var store = new MemStore();
		new JsonExport(db, store).write(mass);
		db.clear();
		new JsonImport(store, db).run();

		var units2 = db.get(UnitGroup.class, units.refId);
		var mass2 = db.get(FlowProperty.class, mass.refId);
		assertNotNull(units2);
		assertNotNull(mass2);
		assertEquals(units2.defaultFlowProperty, mass2);
		assertEquals(mass2.unitGroup, units2);
		db.delete(units2, mass2);
	}

}
