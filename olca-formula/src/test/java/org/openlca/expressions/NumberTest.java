package org.openlca.expressions;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.expressions.FormulaInterpreter;

public class NumberTest {

	private FormulaInterpreter interpreter = new FormulaInterpreter();

	@Test(expected = Throwable.class)
	public void noNumber() throws Exception {
		interpreter.eval("o");
	}

	@Test(expected = Throwable.class)
	public void twoNumbers() throws Exception {
		interpreter.eval("1 2");
	}

	@Test
	public void simpleIntegers() throws Exception {
		assertEquals(4d, interpreter.eval("4"), 1e-20);
		assertEquals(42d, interpreter.eval("42"), 1e-20);
	}

	@Test
	public void simpleDecimals() throws Exception {
		assertEquals(4d, interpreter.eval("4."), 1e-20);
		assertEquals(4.1, interpreter.eval("4.1"), 1e-20);
		assertEquals(0.4, interpreter.eval(".4"), 1e-20);
		assertEquals(0.4, interpreter.eval("0.4"), 1e-20);
	}

	@Test
	public void integersWithExponents() throws Exception {
		assertEquals(4d, interpreter.eval("4e0"), 1e-20);
		assertEquals(4200d, interpreter.eval("42e2"), 1e-20);
		assertEquals(4.20, interpreter.eval("420e-2"), 1e-20);
	}

	@Test
	public void decimalsWithExponents() throws Exception {
		assertEquals(40d, interpreter.eval("4.e1"), 1e-20);
		assertEquals(0.41, interpreter.eval("4.1e-1"), 1e-20);
		assertEquals(40d, interpreter.eval(".4e2"), 1e-20);
		assertEquals(0.004, interpreter.eval("0.4e-2"), 1e-20);
	}

}
