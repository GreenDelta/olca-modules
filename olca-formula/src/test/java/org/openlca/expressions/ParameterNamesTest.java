package org.openlca.expressions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ParameterNamesTest {

	@Test
	public void testParameterNames() throws Exception {
		FormulaInterpreter i = new FormulaInterpreter();
		String[] names = { "a", "_a", "$a", "__a", "_$_a", "_1" };
		for (int k = 0; k < names.length; k++) {
			i.bind(names[k], "42 + " + k);
		}
		for (int k = 0; k < names.length; k++) {
			assertEquals(42 + k, i.eval(names[k]), 1e-16);
		}
	}
}
