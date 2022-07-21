package org.openlca.core.math;

import static org.junit.Assert.*;
import static java.util.Collections.emptySet;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.Uncertainty;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.InterpreterException;

public class FormulaInterpreterTest {

	private final IDatabase db = Tests.getDb();
	private Parameter globalParam;
	private Process process;
	private FormulaInterpreter interpreter;

	/**
	 * sets the following parameters:
	 *
	 * fi_tests_global = 32
	 *
	 * fi_tests_local = fi_tests_global + 10
	 */
	@Before
	public void setUp() {
		globalParam = db.insert(
			Parameter.global("fi_tests_global", 32));
		process = new Process();
		var localParam = Parameter.process(
			"fi_tests_local", "fi_tests_global + 10");
		process.parameters.add(localParam);

		process = db.insert(process);
		interpreter = ParameterTable.interpreter(
			db,
			Collections.singleton(process.id),
			Collections.emptySet());
	}

	@After
	public void tearDown() {
		db.delete(globalParam, process);
	}

	@Test
	public void testEvalLocal() throws Exception {
		var scope = interpreter.getScope(process.id);
		assertTrue(scope.isPresent());
		assertEquals(42, scope.get().eval("fi_tests_local"), 1e-16);
	}

	@Test
	public void testEvalGlobal() throws Exception {
		assertEquals(32, interpreter.eval("fi_tests_global"), 1e-16);
	}

	@Test(expected = InterpreterException.class)
	public void testEvalLocalInGlobalFails() throws Exception {
		assertEquals(42, interpreter.eval("fi_tests_local"), 1e-16);
	}

	@Test
	public void testUncertaintyFormulas() throws Exception {
		var p1 = Parameter.global("p1", 24);
		p1.uncertainty = Uncertainty.triangle(42, 84, 126);
		var p2 = Parameter.global("p2", "p1");
		db.insert(p1, p2);

		// normal interpreter
		var interpreter = ParameterTable.interpreter(db, emptySet(), emptySet());
		assertEquals(interpreter.eval("p2"), 24, 1e-16);

		// simulation
		var simulator = ParameterTable.forSimulation(
			db, Collections.emptySet(), Collections.emptySet());
		for (int i = 0; i < 100; i++) {
			var next = simulator.simulate();
			var value = next.eval("p2");
			assertTrue(value >= 42);
		}

		db.delete(p1, p2);
	}

}
