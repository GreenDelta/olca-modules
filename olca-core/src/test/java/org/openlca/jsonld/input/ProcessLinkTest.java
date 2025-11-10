package org.openlca.jsonld.input;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.output.JsonExport;

public class ProcessLinkTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testProcessLink() {

		var units = db.insert(UnitGroup.of("Units of mass", "kg"));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var p = db.insert(Flow.product("p", mass));
		var pp = db.insert(Process.of("P", p));
		var q = db.insert(Flow.product("q", mass));
		var pq = Process.of("Q", q);
		pq.input(p, 1);
		db.insert(pq);

		var sys = ProductSystem.of(pq);
		sys.link(pp, pq);
		db.insert(sys);

		var memStore = new MemStore();
		new JsonExport(db, memStore).write(sys);
		db.delete(sys, pq, pp, p, q, mass, units);
		new JsonImport(memStore, db).run();

		var sys2 = db.get(ProductSystem.class, sys.refId);
		assertEquals(1, sys2.processLinks.size());

		db.clear();
	}



}
