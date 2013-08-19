package org.openlca.expressions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.expressions.FormulaInterpreter;

public class SumTest {

	private FormulaInterpreter interpreter = new FormulaInterpreter();

	@Test
	public void testEmptySum() throws Exception {
		assertEquals(0d, interpreter.eval("sum()"), 1e-20);
	}

	@Test
	public void testOneValue() throws Exception {
		assertEquals(42.1, interpreter.eval("sum(42.1)"), 1e-20);
	}

	@Test
	public void testManyValues() throws Exception {
		assertEquals(20d, interpreter.eval("sum(2;4;6;8)"), 1e-20);
	}

	@Test
	public void testSumOfSum() throws Exception {
		assertEquals(20d, interpreter.eval("sum( sum(5;5) ; sum(5;5) )"), 1e-20);
	}

	@Test
	public void testWithNegativeValues() throws Exception {
		assertEquals(0d, interpreter.eval("sum( -5.5 ; 5.5 )"), 1e-20);
	}

	@Test(expected = Throwable.class)
	public void testWrongSepartor() throws Exception {
		interpreter.eval("sum(2;4 , 6;8)");
	}

	@Test
	public void testSpellings() throws Exception {
		assertEquals(20d, interpreter.eval("sum(2;4;6;8)"), 1e-20);
		assertEquals(20d, interpreter.eval("Sum(2;4;6;8)"), 1e-20);
		assertEquals(20d, interpreter.eval("SUM(2;4;6;8)"), 1e-20);
	}

}
