package org.openlca.core.matrix;

import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.expressions.FormulaInterpreter;

public class ParameterTableTest {

	@Before
	public void setUp() {
		IDatabase db = Tests.getDb();
		ParameterDao paramDao = new ParameterDao(db);

		Parameter globalInp = inpParameter();
		globalInp.scope = ParameterScope.GLOBAL;
		Parameter globalDep = depParameter();
		globalDep.scope = ParameterScope.GLOBAL;
		paramDao.insert(globalInp);
		paramDao.insert(globalDep);

	}

	private Parameter inpParameter() {
		Parameter inpParam = new Parameter();
		inpParam.setName("inp_param");
		inpParam.isInputParameter = true;
		inpParam.value = 42.0;
		Uncertainty u1 = new Uncertainty();
		u1.distributionType = UncertaintyType.UNIFORM;
		u1.parameter1 = 10.0;
		u1.parameter2 = 21.0;
		inpParam.uncertainty = u1;
		return inpParam;
	}

	private Parameter depParameter() {
		Parameter depParam = new Parameter();
		depParam.setName("dep_param");
		depParam.isInputParameter = false;
		depParam.formula = "2 * inp_param";
		return depParam;
	}

	@After
	public void tearDown() {
		IDatabase db = Tests.getDb();
		new ParameterDao(db).deleteAll();
	}

	@Test
	public void testGlobals() throws Exception {
		FormulaInterpreter fi = ParameterTable2.interpreter(
				Tests.getDb(), emptySet(), emptySet());
		assertEquals(42.0, fi.eval("inp_param"), 1e-6);
		assertEquals(2 * 42.0, fi.eval("dep_param"), 1e-6);
	}

	@Test
	public void testSimulation() throws Exception {
		ParameterTable2 table = ParameterTable2.forSimulation(
				Tests.getDb(), emptySet(), emptySet());
		FormulaInterpreter fi = table.simulate();
		double globalIn = fi.eval("inp_param");
		assertTrue(globalIn > 9 && globalIn < 22);
		assertEquals(2 * globalIn, fi.eval("dep_param"), 1e-6);
	}
}
