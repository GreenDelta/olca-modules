package org.openlca.jsonld.input;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Parameter;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.output.JsonExport;

public class ImpactParameterTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testImpactParameter() {
		var impact = ImpactCategory.of("GWP", "CO2eq");
		impact.parameters.add(Parameter.impact("ch4_fac", 24));
		db.insert(impact);
		var memStore = new MemStore();
		new JsonExport(db, memStore).write(impact);
		db.delete(impact);
		new JsonImport(memStore, db).run();

		var impact2 = db.get(ImpactCategory.class, impact.refId);
		var p = impact2.parameters.get(0);
		assertEquals("ch4_fac", p.name);
		assertEquals(24, p.value, 1e-16);
		assertTrue(p.isInputParameter);
		db.delete(impact2);
	}

}
