package org.openlca.expressions;

import org.junit.Test;
import org.openlca.expressions.Interpreter;
import org.openlca.expressions.InterpreterException;
import org.openlca.expressions.Variable;

public class FailTest {

	private Interpreter interpreter = new Interpreter();

	@Test(expected = InterpreterException.class)
	public void divisionByZero() throws Exception {
		interpreter.bind(new Variable("g10", "5/0"));
		double val = interpreter.eval("g10");
		System.out.println(val);
	}

	@Test(expected = InterpreterException.class)
	public void noOperator() throws Exception {
		interpreter.eval("2 2");
	}

	@Test(expected = InterpreterException.class)
	public void toManyArgs() throws Exception {
		interpreter.eval("sin(3;4;6)");
	}

	@Test(expected = InterpreterException.class)
	public void noClosingBracket() throws Exception {
		interpreter.eval(" ( 1 + 1");
	}
	
	@Test(expected = InterpreterException.class)
	public void unknownVariableName() throws Exception {
		interpreter.eval(" 5 * a");		
	}

}
