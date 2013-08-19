package org.openlca.expressions;

import org.junit.Test;

public class SimpleCycleTest {

	@Test(expected = InterpreterException.class)
	public void test2Nodes() throws Exception {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		interpreter.bind("a", "b");
		interpreter.bind("b", "a");
		interpreter.eval("a");
	}

	@Test(expected = InterpreterException.class)
	public void test3Nodes() throws Exception {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		interpreter.bind("a", "12 * c");
		interpreter.bind("b", "sin(546 * a) / 12^2");
		interpreter.bind("c", "b/189");
		interpreter.eval("c");
	}

	@Test(expected = InterpreterException.class)
	public void test4Nodes() throws Exception {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		interpreter.bind("a", "12 * d");
		interpreter.bind("b", "sin(546 * a) / 12^2");
		interpreter.bind("c", "b/189");
		interpreter.bind("d", "c/189");
		interpreter.eval("d");
	}

}
