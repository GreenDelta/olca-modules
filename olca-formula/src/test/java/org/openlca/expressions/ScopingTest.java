package org.openlca.expressions;

import org.junit.Assert;
import org.junit.Test;

public class ScopingTest {

	@Test
	public void testSimpleScopes() throws Exception {
		// example from
		// http://mitpress.mit.edu/sicp/full-text/book/book-Z-H-21.html#%_sec_3.2
		FormulaInterpreter interpreter = new FormulaInterpreter();

		// global scope
		interpreter.bind("x", "3");
		interpreter.bind("y", "5");

		// I sub-scope of global scope
		Scope a = interpreter.createScope(2);
		a.bind("z", "6");
		a.bind("x", "7");

		// II sub-scope of global scope
		Scope b = interpreter.createScope(3);
		b.bind("n", "1");
		b.bind("y", "2");

		Assert.assertEquals(3.0, interpreter.eval("x"), 1e-16);
		Assert.assertEquals(7.0, a.eval("x"), 1e-16);
		Assert.assertEquals(3.0, b.eval("x"), 1e-16);

		Assert.assertEquals(5.0, interpreter.eval("y"), 1e-16);
		Assert.assertEquals(5.0, a.eval("y"), 1e-16);
		Assert.assertEquals(2.0, b.eval("y"), 1e-16);

	}

	@Test
	public void testSearchInParentScope() throws Exception {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		interpreter.bind("a", "b + 5");
		interpreter.bind("b", "4");
		Scope scope = interpreter.createScope(1);
		scope.bind("c", "a * 2");
		scope.bind("b", "a");
		Assert.assertEquals(18.0, scope.eval("c"), 1e-16);
	}

}
