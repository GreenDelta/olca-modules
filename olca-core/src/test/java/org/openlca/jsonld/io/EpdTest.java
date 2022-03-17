package org.openlca.jsonld.io;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class EpdTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testWithProduct() {
		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		var product = Flow.product("Product", mass);
		var epd = Epd.of("EPD", product);
		db.insert(units, mass, product, epd);

		var store = new MemStore();
		new JsonExport(db, store).write(epd);
		db.clear();
		new JsonImport(store, db).run();

		var clone = db.get(Epd.class, epd.refId);
		assertEquals("EPD", clone.name);
		assertEquals("Product", clone.product.flow.name);
		assertEquals("Mass", clone.product.property.name);
		assertEquals("kg", clone.product.unit.name);

		db.clear();
	}

}
