package org.openlca.core.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.InterpreterException;
import org.openlca.expressions.Scope;

public class FormulaInterpretersTest {

	private IDatabase database = Tests.getDb();
	private Parameter globalParam;
	private Process process;
	private ParameterTable parameterTable;
	private FormulaInterpreter interpreter;

	/**
	 * sets the following parameters:
	 * 
	 * fi_tests_global = 32
	 * 
	 * fi_tests_local = fi_tests_global + 10
	 */
	@Before
	public void setUp() throws Exception {
		globalParam = new Parameter();
		globalParam.setName("fi_tests_global");
		globalParam.setInputParameter(true);
		globalParam.setScope(ParameterScope.GLOBAL);
		globalParam.setValue(32);
		database.createDao(Parameter.class).insert(globalParam);
		process = new Process();
		Parameter localParam = new Parameter();
		localParam.setName("fi_tests_local");
		localParam.setFormula("fi_tests_global + 10");
		localParam.setInputParameter(false);
		localParam.setScope(ParameterScope.PROCESS);
		process.getParameters().add(localParam);
		process = database.createDao(Process.class).insert(process);
		Set<Long> context = Collections.singleton(process.getId());
		parameterTable = ParameterTable.build(database, context);
		interpreter = parameterTable.createInterpreter();
	}

	@After
	public void tearDown() throws Exception {
		database.createDao(Parameter.class).delete(globalParam);
		database.createDao(Process.class).delete(process);
	}

	@Test
	public void testEvalLocal() throws Exception {
		Scope scope = interpreter.getScope(process.getId());
		Assert.assertEquals(42, scope.eval("fi_tests_local"), 1e-16);
	}

	@Test
	public void testEvalGlobal() throws Exception {
		Assert.assertEquals(32, interpreter.eval("fi_tests_global"), 1e-16);
	}

	@Test(expected = InterpreterException.class)
	public void testEvalLocalInGlobalFails() throws Exception {
		Assert.assertEquals(42, interpreter.eval("fi_tests_local"), 1e-16);
	}

	@Test
	public void testRedefine() throws Exception {
		List<ParameterRedef> redefs = new ArrayList<>();
		redefs.add(new ParameterRedef() {
			{
				setName("fi_tests_global");
				setValue(3.1);
			}
		});
		redefs.add(new ParameterRedef() {
			{
				setName("fi_tests_local");
				setValue(1.3);
				setContextId(process.getId());
			}
		});
		parameterTable.apply(redefs);
		interpreter = parameterTable.createInterpreter();
		Assert.assertEquals(3.1, interpreter.eval("fi_tests_global"), 1e-16);
		Scope scope = interpreter.getScope(process.getId());
		// assure that the formula was deleted
		Assert.assertEquals(1.3, scope.eval("fi_tests_local"), 1e-16);
	}
}
