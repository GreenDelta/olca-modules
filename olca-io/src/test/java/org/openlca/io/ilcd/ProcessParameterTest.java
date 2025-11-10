package org.openlca.io.ilcd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.io.MemDataStore;
import org.openlca.ilcd.util.Processes;
import org.openlca.io.Tests;
import org.openlca.io.ilcd.output.Export;
import org.openlca.io.ilcd.output.ProcessExport;

public class ProcessParameterTest {

	private final IDatabase db = Tests.getDb();

	private Parameter global1;
	private Parameter global2;
	private Process process;

	@Before
	public void setup() {
		global1 = Parameter.global("g1", 42);
		global2 = Parameter.global("g2", "2 * g1");
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var product = Flow.product("p", mass);
		process = Process.of("process", product);
		db.insert(
				global1,
				global2,
				units,
				mass,
				product,
				process);
	}

	@After
	public void cleanup() {
		var removals = List.of(
				global1,
				global2,
				process,
				process.quantitativeReference.flow,
				process.quantitativeReference.flow.referenceFlowProperty,
				process.quantitativeReference.flow.referenceFlowProperty.unitGroup);
		for (var r : removals) {
			db.delete(r);
		}
	}

	@Test
	public void testNoParams()  {
		var p = conv();
		assertTrue(Processes.getParameters(p).isEmpty());
	}

	@Test
	public void testOnlyLocal() {
		var local = Parameter.process("local1", 1.0);
		process.parameters.add(local);
		process = db.update(process);
		var p = conv();
		assertEquals(1, Processes.getParameters(p).size());
		assertEquals("local1", Processes.getParameters(p).getFirst().getName());
	}

	@Test
	public void testGlobals() {
		var local = Parameter.process("local1", 1.0);
		process.parameters.add(local);
		process.quantitativeReference.formula = "1 / g2";
		process = db.update(process);

		// note that the size of the converted parameters can
		// be larger than 3 because it may generate a temporary
		// parameter for the formula
		var conv = Processes.getParameters( conv())
				.stream()
				.map(org.openlca.ilcd.processes.Parameter::getName)
				.toList();

		assertTrue(conv.contains("local1"));
		assertTrue(conv.contains("g1"));
		assertTrue(conv.contains("g2"));
	}

	private org.openlca.ilcd.processes.Process conv() {
		try (var store = new MemDataStore()) {
			new ProcessExport(new Export(db, store), process).write();
			var p = store.get(org.openlca.ilcd.processes.Process.class, process.refId);
			assertNotNull(p);
			return p;
		} catch (Exception e) {
			throw new RuntimeException("failed to convert process", e);
		}
	}

}
