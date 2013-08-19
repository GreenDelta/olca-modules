package org.openlca.expressions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.expressions.FormulaInterpreter;

public class ETest {

	@Test
	public void testSingleE() throws Exception {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		assertEquals(Math.E, interpreter.eval("e"), 1e-16);
		assertEquals(0, interpreter.eval("e-e"), 1e-16);
		assertEquals(Math.E * 2, interpreter.eval("e+e"), 1e-16);
		assertEquals(Math.E * 3, interpreter.eval("2*e+e"), 1e-16);
		assertEquals(Math.E, interpreter.eval("E"), 1e-16);
		assertEquals(0, interpreter.eval("E-E"), 1e-16);
		assertEquals(Math.E * 2, interpreter.eval("E+E"), 1e-16);
		assertEquals(Math.E * 3, interpreter.eval("2*E+E"), 1e-16);
	}

	@Test
	public void testNoSignExp() throws Exception {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		assertEquals(1, interpreter.eval("1E0"), 1e-16);
		assertEquals(10, interpreter.eval("10E0"), 1e-16);
		assertEquals(11, interpreter.eval("10E0+1"), 1e-16);
		assertEquals(101, interpreter.eval("1E2+1"), 1e-16);
		assertEquals(9, interpreter.eval("10E0-1"), 1e-16);
		assertEquals(99, interpreter.eval("1E2-1"), 1e-16);
	}

	@Test
	public void testPositiveExp() throws Exception {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		assertEquals(1, interpreter.eval("1E+0"), 1e-16);
		assertEquals(10, interpreter.eval("10E+0"), 1e-16);
		assertEquals(11, interpreter.eval("10E0+1"), 1e-16);
		assertEquals(101, interpreter.eval("1E+2+1"), 1e-16);
		assertEquals(9, interpreter.eval("10E+0-1"), 1e-16);
		assertEquals(99, interpreter.eval("1E+2-1"), 1e-16);
	}

	@Test
	public void testNegativeExp() throws Exception {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		assertEquals(1, interpreter.eval("1E-0"), 1e-16);
		assertEquals(10, interpreter.eval("10E-0"), 1e-16);
		assertEquals(11, interpreter.eval("10E-0+1"), 1e-16);
		assertEquals(1.01, interpreter.eval("1E-2+1"), 1e-16);
		assertEquals(9, interpreter.eval("10E-0-1"), 1e-16);
		assertEquals(0.01, interpreter.eval("1E-2"), 1e-16);
	}

}
