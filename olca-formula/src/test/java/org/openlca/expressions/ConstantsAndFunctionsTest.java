package org.openlca.expressions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConstantsAndFunctionsTest {

	@Test
	public void testConstants() throws Exception {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		assertEquals(Math.E, interpreter.eval("e"), 1e-10);
		assertEquals(Math.E, interpreter.eval("E"), 1e-10);
		assertEquals(Math.PI, interpreter.eval("pi"), 1e-10);
		assertEquals(Math.PI, interpreter.eval("Pi"), 1e-10);
		assertEquals(Math.PI, interpreter.eval("PI"), 1e-10);
		assertEquals(1, interpreter.eval("if(true;1;0)"), 1e-10);
		assertEquals(1, interpreter.eval("if(True;1;0)"), 1e-10);
		assertEquals(1, interpreter.eval("if(TRUE;1;0)"), 1e-10);
		assertEquals(0, interpreter.eval("if(false;1;0)"), 1e-10);
		assertEquals(0, interpreter.eval("if(False;1;0)"), 1e-10);
		assertEquals(0, interpreter.eval("if(FALSE;1;0)"), 1e-10);
	}

	@Test
	public void testFunctions() throws Exception {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		assertEquals(Math.E, interpreter.eval("e()"), 1e-10);
		assertEquals(Math.E, interpreter.eval("E()"), 1e-10);
		assertEquals(Math.PI, interpreter.eval("pi()"), 1e-10);
		assertEquals(Math.PI, interpreter.eval("Pi()"), 1e-10);
		assertEquals(Math.PI, interpreter.eval("PI()"), 1e-10);
		assertEquals(1, interpreter.eval("if(true();1;0)"), 1e-10);
		assertEquals(1, interpreter.eval("if(True();1;0)"), 1e-10);
		assertEquals(1, interpreter.eval("if(TRUE();1;0)"), 1e-10);
		assertEquals(0, interpreter.eval("if(false();1;0)"), 1e-10);
		assertEquals(0, interpreter.eval("if(False();1;0)"), 1e-10);
		assertEquals(0, interpreter.eval("if(FALSE();1;0)"), 1e-10);
	}

}
