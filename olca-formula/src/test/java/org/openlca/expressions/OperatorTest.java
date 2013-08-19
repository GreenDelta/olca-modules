package org.openlca.expressions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.expressions.FormulaInterpreter;

public class OperatorTest {

	private FormulaInterpreter interpreter = new FormulaInterpreter();

	@Test
	public void testPlus() throws Exception {
		assertEquals(2.0, interpreter.eval("+1+1"), 1e-20);
		assertEquals(2.0, interpreter.eval("1+1"), 1e-20);
		assertEquals(2.5, interpreter.eval("1+1+0.5"), 1e-20);
		assertEquals(3.5, interpreter.eval("1+sum(1.5 + 1 + sum())"), 1e-20);
	}

	@Test
	public void testMinus() throws Exception {
		assertEquals(-3.0, interpreter.eval("-3"), 1e-20);
		assertEquals(3.0, interpreter.eval("-(-3)"), 1e-20);
		assertEquals(-3.0, interpreter.eval("-3+1-1"), 1e-20);
		assertEquals(2.0, interpreter.eval("3-1"), 1e-20);
		assertEquals(2.5, interpreter.eval("5-2-0.5"), 1e-20);
		assertEquals(8.5, interpreter.eval("7-sum(-1.5 - 1 - sum(-1))"), 1e-20);
	}

	@Test
	public void testPower() throws Exception {
		assertEquals(8.0, interpreter.eval("2^3"), 1e-20);
		assertEquals(16.0, interpreter.eval("2^2^2"), 1e-20);
		assertEquals(16.0, interpreter.eval("(2+2)^2"), 1e-20);
		assertEquals(5.0, interpreter.eval("1+2^2"), 1e-20);
		assertEquals(-1.0, interpreter.eval("- 2  ^  - 3  *  8 "), 1e-20);
	}

	@Test
	public void testDivision() throws Exception {
		assertEquals(1.0, interpreter.eval("2/2"), 1e-20);
	}

	@Test
	public void testIntDivision() throws Exception {
		assertEquals(10.0, interpreter.eval("2 * (7 div 2 + 2)"), 1e-20);
	}

}
