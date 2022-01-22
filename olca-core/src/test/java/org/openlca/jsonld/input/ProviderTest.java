package org.openlca.jsonld.input;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.output.JsonExport;

public class ProviderTest {

	private final IDatabase db = Tests.getDb();

	@After
	public void cleanup() {
		db.clear();
	}

	@Test
	public void testResolveFromImport() {

		var db = Tests.getDb();

		// reference data and process 1
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var prod1 = Flow.product("prod1", mass);
		var prod2 = Flow.product("prod2", mass);
		var proc1 = Process.of("proc1", prod1);
		db.insert(units, mass, prod1, prod2, proc1);

		// process 2 with process 1 as provider
		var proc2 = Process.of("proc2", prod2);
		proc2.input(prod1, 2.0).defaultProviderId = proc1.id;
		db.insert(proc2);

		// export to json and clear DB
		var store = new MemStore();
		var export = new JsonExport(db, store);
		List.of(units, mass, prod1, prod2, proc1, proc2)
			.forEach(export::write);
		db.clear();

		// import and check provider
		var imp = new JsonImport(store, db);
		imp.run(ModelType.PROCESS, proc2.refId);
		imp.run(ModelType.PROCESS, proc1.refId);

		var p1 = db.get(Process.class, proc1.refId);
		var p2 = db.get(Process.class, proc2.refId);
		var e = p2.exchanges.stream()
			.filter(ex -> ex.defaultProviderId == p1.id)
			.findAny()
			.orElse(null);

		assertNotNull(e);
		assertEquals(2.0, e.amount, 1e-10);
		assertEquals(prod1.refId, e.flow.refId);
		db.clear();
	}
}
