package org.openlca.expressions;

import org.junit.Test;
import org.openlca.expressions.Interpreter;
import org.openlca.expressions.InterpreterException;
import org.openlca.expressions.Variable;

public class SimpleCycleTest {

	@Test(expected = InterpreterException.class)
	public void test2Nodes() throws Exception {
		Interpreter interpreter = new Interpreter();
		Variable a = new Variable("a", "b");
		Variable b = new Variable("b", "a");
		interpreter.bind(a);
		interpreter.bind(b);
		interpreter.evalVariables();
	}
	
	@Test(expected = InterpreterException.class)
	public void test3Nodes() throws Exception {
		Interpreter interpreter = new Interpreter();
		Variable a = new Variable("a", "12 * c");
		Variable b = new Variable("b", "sin(546 * a) / 12^2");
		Variable c = new Variable("c", "b/189");
		interpreter.bind(a);
		interpreter.bind(b);
		interpreter.bind(c);
		interpreter.evalVariables();
	}
	
	@Test(expected = InterpreterException.class)
	public void test4Nodes() throws Exception {
		Interpreter interpreter = new Interpreter();
		Variable a = new Variable("a", "12 * d");
		Variable b = new Variable("b", "sin(546 * a) / 12^2");
		Variable c = new Variable("c", "b/189");
		Variable d = new Variable("d", "c/189");
		interpreter.bind(a);
		interpreter.bind(b);
		interpreter.bind(c);
		interpreter.bind(d);
		interpreter.evalVariables();
	}
	
}
