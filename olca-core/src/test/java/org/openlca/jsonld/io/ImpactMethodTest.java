package org.openlca.jsonld.io;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class ImpactMethodTest extends AbstractZipTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testEmptyMethod() {
		var method = db.insert(ImpactMethod.of("Some method"));
		with(zip -> new JsonExport(db, zip).write(method));
		db.delete(method);
		assertNull(db.get(ImpactMethod.class, method.refId));
		with(zip -> new JsonImport(zip, db).run());
		var clone = db.get(ImpactMethod.class, method.refId);
		assertEquals(method.name, clone.name);
		db.delete(method);
	}

	@Test
	public void testSharedCategories() {

		// build and export the model
		var impact = db.insert(ImpactCategory.of("GWP", "kg CO2 eq."));
		var method1 = ImpactMethod.of("Method 1");
		method1.impactCategories.add(impact);
		var method2 = ImpactMethod.of("Method 2");
		method2.impactCategories.add(impact);
		db.insert(method1, method2);
		with(zip -> {
			var export = new JsonExport(db, zip);
			export.write(method1);
			export.write(method2);
		});

		db.delete(method2, method1, impact);

		// import and check
		with(zip -> new JsonImport(zip, db).run());
		var m1 = db.get(ImpactMethod.class, method1.refId);
		var m2 = db.get(ImpactMethod.class, method2.refId);
		var c1 = m1.impactCategories.get(0);
		var c2 = m2.impactCategories.get(0);
		assertEquals(c1, c2);
		db.delete(m1, m2, c1);
	}

}
