package org.openlca.jsonld.input;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.output.JsonExport;

import static org.junit.Assert.assertEquals;


public class UnitGroupImportTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testUpdateUnit() {

		// create and export the unit group
		var group = db.insert(UnitGroup.of("Mass units", Unit.of("kg")));
		var store = new MemStore();
		new JsonExport(db, store).write(group);

		// update the JSON object
		var json = store.get(ModelType.UNIT_GROUP, group.refId);
		json.addProperty("description", "unit group description");
		json.addProperty("version", "42.0.0");
		var units = Json.getArray(json, "units");
		var unit = units.get(0).getAsJsonObject();
		unit.addProperty("description", "unit description");

		// update it
		new JsonImport(store, db)
				.setUpdateMode(UpdateMode.IF_NEWER)
				.run();
		var updated = db.get(UnitGroup.class, group.id);
		db.delete(updated);

		// check updated fields
		assertEquals("unit group description", updated.description);
		assertEquals("unit description", updated.referenceUnit.description);
	}
}
