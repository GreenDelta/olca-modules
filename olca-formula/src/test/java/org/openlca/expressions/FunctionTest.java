package org.openlca.expressions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FunctionTest {

	private FormulaInterpreter interpreter = new FormulaInterpreter();

	@Test
	public void testSample1() throws Exception {
		interpreter.getGlobalScope().bind("g2", "1");
		interpreter.bind("g3", "2");
		interpreter.bind("g4", "3");
		interpreter.bind("g5", "g3^4");
		interpreter.bind("g6", "g4-g2");
		interpreter.bind("g7", "g4/g3");
		assertEquals(1, interpreter.eval("g2"), 1e-20);
		assertEquals(2, interpreter.eval("g3"), 1e-20);
		assertEquals(3, interpreter.eval("g4"), 1e-20);
		assertEquals(16, interpreter.eval("g5"), 1e-20);
		assertEquals(2, interpreter.eval("g6"), 1e-20);
		assertEquals(1.5, interpreter.eval("g7"), 1e-20);
	}

	@Test
	public void testSample2() throws Exception {
		interpreter.bind("g2", "1");
		interpreter.bind("g3", "2");
		interpreter.bind("g4", "3");
		interpreter.bind("g8", "g4 div g3");
		interpreter.bind("g9", "(g2+g3+6) mod 2 ");
		interpreter.bind("g10", "abs(-g4) ");
		interpreter.bind("g11", "acos(-g2)");
		assertEquals(1, interpreter.eval("g2"), 1e-20);
		assertEquals(2, interpreter.eval("g3"), 1e-20);
		assertEquals(3, interpreter.eval("g4"), 1e-20);
		assertEquals(1, interpreter.eval("g8"), 1e-20);
		assertEquals(1, interpreter.eval("g9"), 1e-20);
		assertEquals(3, interpreter.eval("g10"), 1e-20);
		assertEquals(3.14159265, interpreter.eval("g11"), 1e-4);
	}

	@Test
	public void testSample3() throws Exception {
		interpreter.bind("g5", "16");
		interpreter.bind("g6", "g5-2");
		interpreter.bind("g7", "g5/g5");
		interpreter.bind("g11", "acos(-1)");
		interpreter.bind("g50", "ln((0.2+5*10^-3)/(0.1-5*10^-3))");
		interpreter.bind("g51", "9.25+ln((0.2+5*10^-3)/(0.1-5*10^-3))");
		assertEquals(16, interpreter.eval("g5"), 1e-20);
		assertEquals(14, interpreter.eval("g6"), 1e-20);
		assertEquals(1, interpreter.eval("g7"), 1e-20);
		assertEquals(3.14159265, interpreter.eval("g11"), 1e-4);
		assertEquals(0.769133087537867, interpreter.eval("g50"), 1e-10);
		assertEquals(10.0191330875379, interpreter.eval("g51"), 1e-10);
	}

	@Test
	public void testSample4() throws Exception {
		interpreter.bind("g12", "20.64159265");
		interpreter.bind("g13", "exp(g12)");
		interpreter.bind("g14", "log(g12)");
		assertEquals(20.64159265, interpreter.eval("g12"), 1e-20);
		assertEquals(921573091.602758, interpreter.eval("g13"), 1e-5);
		assertEquals(1.31474320324693, interpreter.eval("g14"), 1e-5);
	}

	@Test
	public void testSample5() throws Exception {
		interpreter.bind("g5", "16");
		interpreter.bind("g21", "sin(g5)");
		interpreter.bind("g22", "sqr(g5)");
		interpreter.bind("g23", "sqrt(256)");
		interpreter.bind("g28", "if(g5>10;g5;0)");
		assertEquals(16, interpreter.eval("g5"), 1e-20);
		assertEquals(-0.287903317, interpreter.eval("g21"), 1e-5);
		assertEquals(256, interpreter.eval("g22"), 1e-20);
		assertEquals(16, interpreter.eval("g23"), 1e-20);
		assertEquals(16, interpreter.eval("g28"), 1e-20);
	}

}
