package org.openlca.jsonld.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProviderType;
import org.openlca.core.model.Result;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class ResultAsProviderTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testResultAsProvider() {

		// create the reference data & the result
		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("p", mass);
		var r = Flow.product("r", mass);
		var R = Result.of("R", r);
		db.insert(units, mass, p, r, R);

		// create the process
		var P = Process.of("P", p);
		var input = P.input(r, 42);
		input.defaultProviderId = R.id;
		input.defaultProviderType = ProviderType.RESULT;
		db.insert(P);

		// export, clear, and reimport
		var store = new MemStore();
		new JsonExport(db, store)
				.withDefaultProviders(true)
				.write(P);
		db.delete(P, R, r, p, mass, units);
		new JsonImport(store, db)
				.run();

		// check the result
		P = db.get(Process.class, P.refId);
		assertNotNull(P);
		input = P.exchanges.stream()
				.filter(e -> e.isInput)
				.findFirst()
				.orElseThrow();
		assertEquals(42, input.amount, 1e-16);

		assertEquals(ProviderType.RESULT, input.defaultProviderType);
		R = db.get(Result.class, input.defaultProviderId);
		assertNotNull(R);
		assertEquals("r", R.referenceFlow.flow.name);

		// delete the data
		db.delete(
				P,
				R,
				P.quantitativeReference.flow,
				R.referenceFlow.flow,
				R.referenceFlow.flow.referenceFlowProperty,
				R.referenceFlow.flow.referenceFlowProperty.unitGroup);
	}
}
