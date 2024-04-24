package org.openlca.jsonld.io;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Source;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class ImpactCategoryTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testSource() {
		var source = Source.of("Source");
		var impact = ImpactCategory.of("I", "kg eq.");
		impact.source = source;
		db.insert(source, impact);

		var store = new MemStore();
		new JsonExport(db, store).write(impact);
		db.delete(impact, source);
		new JsonImport(store, db).run();

		var copy = db.get(ImpactCategory.class, impact.refId);
		assertNotNull(copy);
		assertNotNull(copy.source);
		assertEquals(source.refId, copy.source.refId);
	}
}
