package org.openlca.core.matrix;

import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.expressions.FormulaInterpreter;

public class ParameterTableTest {

	private Process process;

	@Before
	public void setUp() {
		IDatabase db = Tests.getDb();

		Parameter globalInp = inpParameter(42);
		globalInp.scope = ParameterScope.GLOBAL;
		Parameter globalDep = depParameter(2);
		globalDep.scope = ParameterScope.GLOBAL;
		ParameterDao paramDao = new ParameterDao(db);
		paramDao.insert(globalInp);
		paramDao.insert(globalDep);

		process = new Process();
		Parameter ppInp = inpParameter(84);
		ppInp.scope = ParameterScope.PROCESS;
		Parameter ppDep = depParameter(3);
		ppDep.scope = ParameterScope.PROCESS;
		process.parameters.add(ppInp);
		process.parameters.add(ppDep);
		new ProcessDao(db).insert(process);

	}

	private Parameter inpParameter(double val) {
		Parameter inpParam = new Parameter();
		inpParam.name = "inp_param";
		inpParam.isInputParameter = true;
		inpParam.value = val;
		Uncertainty u1 = new Uncertainty();
		u1.distributionType = UncertaintyType.UNIFORM;
		u1.parameter1 = 10.0;
		u1.parameter2 = val / 2;
		inpParam.uncertainty = u1;
		return inpParam;
	}

	private Parameter depParameter(double factor) {
		Parameter depParam = new Parameter();
		depParam.name = "dep_param";
		depParam.isInputParameter = false;
		depParam.formula = factor + " * inp_param";
		return depParam;
	}

	@After
	public void tearDown() {
		IDatabase db = Tests.getDb();
		new ParameterDao(db).deleteAll();
		new ProcessDao(db).deleteAll();
	}

	@Test
	public void testGlobals() throws Exception {
		FormulaInterpreter fi = ParameterTable.interpreter(
				Tests.getDb(), emptySet(), emptySet());
		assertEquals(42.0, fi.eval("inp_param"), 1e-6);
		assertEquals(2 * 42.0, fi.eval("dep_param"), 1e-6);
	}

	@Test
	public void testProcessParams() throws Exception {
		FormulaInterpreter fi = ParameterTable.interpreter(Tests.getDb(),
				Collections.singleton(process.id), emptySet());
		// global
		assertEquals(42.0, fi.eval("inp_param"), 1e-6);
		assertEquals(2 * 42.0, fi.eval("dep_param"), 1e-6);
		// local
		var scope = fi.getScope(process.id);
		assertTrue(scope.isPresent());
		assertEquals(84.0, scope.get().eval("inp_param"), 1e-6);
		assertEquals(3 * 84.0, scope.get().eval("dep_param"), 1e-6);
	}

	@Test
	public void testSimulation() throws Exception {
		var table = ParameterTable.forSimulation(
				Tests.getDb(), emptySet(), emptySet());
		var fi = table.simulate();
		double globalIn = fi.eval("inp_param");
		assertTrue(globalIn > 9 && globalIn < 22);
		assertEquals(2 * globalIn, fi.eval("dep_param"), 1e-6);
	}

	@Test
	public void testProcessSimulation() throws Exception {
		ParameterTable table = ParameterTable.forSimulation(Tests.getDb(),
				Collections.singleton(process.id), emptySet());

		// global
		var fi = table.simulate();
		double globalIn = fi.eval("inp_param");
		assertTrue(globalIn > 9 && globalIn < 22);
		assertEquals(2 * globalIn, fi.eval("dep_param"), 1e-6);

		// local
		var scope = fi.getScope(process.id);
		assertTrue(scope.isPresent());
		double ppInp = scope.get().eval("inp_param");
		assertTrue(ppInp > 9 && ppInp < 84. / 2.);
		assertEquals(3 * ppInp, scope.get().eval("dep_param"), 1e-6);
	}

	@Test
	public void testGloablRedef() throws Exception {
		ParameterRedef redef = new ParameterRedef();
		redef.name = "inp_param";
		redef.value = 99;

		var fi = ParameterTable.interpreter(
				Tests.getDb(),
				Collections.singleton(process.id),
				Collections.singleton(redef));

		// global
		assertEquals(99.0, fi.eval("inp_param"), 1e-6);
		assertEquals(2 * 99.0, fi.eval("dep_param"), 1e-6);

		// local
		var scope = fi.getScope(process.id);
		assertTrue(scope.isPresent());
		assertEquals(84.0, scope.get().eval("inp_param"), 1e-6);
		assertEquals(3 * 84.0, scope.get().eval("dep_param"), 1e-6);
	}

	@Test
	public void testLocalRedef() throws Exception {
		var redef = new ParameterRedef();
		redef.contextId = process.id;
		redef.contextType = ModelType.PROCESS;
		redef.name = "inp_param";
		redef.value = 99;

		var fi = ParameterTable.interpreter(Tests.getDb(),
				Collections.singleton(process.id),
				Collections.singleton(redef));

		// global
		assertEquals(42.0, fi.eval("inp_param"), 1e-6);
		assertEquals(2 * 42.0, fi.eval("dep_param"), 1e-6);

		// local
		var scope = fi.getScope(process.id);
		assertTrue(scope.isPresent());
		assertEquals(99.0, scope.get().eval("inp_param"), 1e-6);
		assertEquals(3 * 99.0, scope.get().eval("dep_param"), 1e-6);
	}

	@Test
	public void testGlobalRedefSimulation() throws Exception {
		var redef = new ParameterRedef();
		redef.name = "inp_param";
		redef.value = 99;
		redef.uncertainty = Uncertainty.uniform(1001, 2000);

		var table = ParameterTable.forSimulation(Tests.getDb(),
				Collections.singleton(process.id),
				Collections.singleton(redef));

		// global
		var fi = table.simulate();
		double globalIn = fi.eval("inp_param");
		assertTrue(globalIn > 1000);
		assertEquals(2 * globalIn, fi.eval("dep_param"), 1e-6);

		// local
		var scope = fi.getScope(process.id);
		assertTrue(scope.isPresent());
		double ppInp = scope.get().eval("inp_param");
		assertTrue(ppInp > 9 && ppInp < 84. / 2.);
		assertEquals(3 * ppInp, scope.get().eval("dep_param"), 1e-6);
	}

	@Test
	public void testLocalRedefSimulation() throws Exception {
		var redef = new ParameterRedef();
		redef.contextId = process.id;
		redef.contextType = ModelType.PROCESS;
		redef.name = "inp_param";
		redef.value = 99;
		redef.uncertainty = Uncertainty.uniform(1001, 2000);

		var table = ParameterTable.forSimulation(
				Tests.getDb(),
				Collections.singleton(process.id),
				Collections.singleton(redef));
		var fi = table.simulate();

		// global
		double globalIn = fi.eval("inp_param");
		assertTrue(globalIn > 9 && globalIn < 22);
		assertEquals(2 * globalIn, fi.eval("dep_param"), 1e-6);

		// local
		var scope = fi.getScope(process.id);
		assertTrue(scope.isPresent());
		double ppInp = scope.get().eval("inp_param");
		assertTrue(ppInp > 1000);
		assertEquals(3 * ppInp, scope.get().eval("dep_param"), 1e-6);
	}
}
