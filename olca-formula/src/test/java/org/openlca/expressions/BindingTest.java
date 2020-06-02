package org.openlca.expressions;

import org.junit.Assert;
import org.junit.Test;

public class BindingTest {

	@Test
	public void testLocalBindings() throws Exception {
		var interpreter = new FormulaInterpreter();
		var localScope = interpreter.createScope(1);
		localScope.bind("a", 21);
		localScope.bind("b", "2");
		Assert.assertEquals(42.0, localScope.eval("a * b"), 1e-16);
	}

	@Test
	public void testGlobalBindings() throws Exception {
		var interpreter = new FormulaInterpreter();
		interpreter.bind("a", 21);
		interpreter.bind("b", "2");
		Assert.assertEquals(42.0, interpreter.eval("a * b"), 1e-16);
	}

	@Test
	public void testMixedBindings() throws Exception {
		var interpreter = new FormulaInterpreter();
		var localScope = interpreter.createScope(1);
		interpreter.bind("a", 21);
		localScope.bind("b", "2");
		Assert.assertEquals(42.0, localScope.eval("a * b"), 1e-16);
	}
}
