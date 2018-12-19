package org.openlca.core.matrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

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

		Parameter globalIn = new Parameter();
		globalIn.scope = ParameterScope.GLOBAL;
		globalIn.setName("globalIn");
		globalIn.isInputParameter = true;
		globalIn.value = 42.0;
		Uncertainty u1 = new Uncertainty();
		u1.distributionType = UncertaintyType.NORMAL;
		u1.parameter1 = 42.0;
		u1.parameter2 = 1.0;
		globalIn.uncertainty = u1;
		paramDao.insert(globalIn);
	}

	@After
	public void tearDown() {
		IDatabase db = Tests.getDb();
		new ParameterDao(db).deleteAll();
	}

	@Test
	public void testGlobals() throws Exception {
		ParameterTable table = ParameterTable.build(
				Tests.getDb(), Collections.emptySet());
		FormulaInterpreter fi = table.createInterpreter();
		assertEquals(42.0, fi.eval("globalIn"), 1e-6);

		table.simulate();
		fi = table.createInterpreter();
		assertTrue(42.0 != fi.eval("globalIn"));
	}
}
