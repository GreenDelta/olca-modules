package org.openlca.expressions;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.expressions.FormulaInterpreter;

/**
 * Evaluates the expressions from the specification.
 */
public class SpecificationTest {

	@Test
	public void testSpecification() throws Exception {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		assertEquals(Math.PI, interpreter.eval("pi"), 1e-15);
		assertEquals(Math.E, interpreter.eval("e"), 1e-15);
		assertEquals(-1, interpreter.eval("-1.0"), 1e-15);
		assertEquals(1, interpreter.eval("-(-1)"), 1e-15);
		assertEquals(-0.125, interpreter.eval("-2^-3"), 1e-15);
		assertEquals(-4, interpreter.eval("2 *-2"), 1e-15);
		assertEquals(1.5, interpreter.eval("6/2/2"), 1e-15);
		assertEquals(2, interpreter.eval("12 div 5"), 1e-15);
		assertEquals(3, interpreter.eval("13 mod 5"), 1e-15);
		assertEquals(2, interpreter.eval("4 + -2"), 1e-15);
		assertEquals(-3, interpreter.eval("3 - 6"), 1e-15);
		assertEquals(2, interpreter.eval("if(3 = 3;2;0)"), 1e-15);
		assertEquals(0, interpreter.eval("if(3 == 4;2;0)"), 1e-15);
		assertEquals(8, interpreter.eval("if(3 <> 4; 8; 3)"), 1e-15);
		assertEquals(3, interpreter.eval("if(5 != 5; 8; 3)"), 1e-15);
		assertEquals(2, interpreter.eval("if(-4 < -5; 4; 2)"), 1e-15);
		assertEquals(4, interpreter.eval("if(3 <= 3.0; 4;2)"), 1e-15);
		assertEquals(4, interpreter.eval("if(3 > -3; 4; 2)"), 1e-15);
		assertEquals(4, interpreter.eval("if(3 >=-3; 4; 2)"), 1e-15);
		assertEquals(4, interpreter.eval("if(3 > 2 & 4 > 3; 4; 2)"), 1e-15);
		assertEquals(2, interpreter.eval("if(3 > 2 && 4 < 3; 4; 2)"), 1e-15);
		assertEquals(2, interpreter.eval("if(3 < 2 | 4 < 3; 4; 2)"), 1e-15);
		assertEquals(4, interpreter.eval("if(3 > 2 || 4 < 3; 4; 2)"), 1e-15);
		assertEquals(2, interpreter.eval("abs(-(-(-2)))"), 1e-15);
		assertEquals(Math.PI, interpreter.eval("acos(-1)"), 1e-15);
		assertEquals(Math.PI, interpreter.eval("arccos(-1)"), 1e-15);
		assertEquals(4, interpreter.eval("if(and(3>2;4>3;5>4);4;2)"), 1e-15);
		assertEquals(-Math.PI / 2, interpreter.eval("asin(-1)"), 1e-15);
		assertEquals(-Math.PI / 2, interpreter.eval("arcsin(-1)"), 1e-15);
		assertEquals(-Math.PI / 4, interpreter.eval("atan(-1)"), 1e-15);
		assertEquals(-Math.PI / 4, interpreter.eval("arctan(-1)"), 1e-15);
		assertEquals(3, interpreter.eval("avg(1;2;3;4;5)"), 1e-15);
		assertEquals(3, interpreter.eval("mean(1;2;3;4;5)"), 1e-15);
		assertEquals(3, interpreter.eval("ceil(2 + 0.2)"), 1e-15);
		assertEquals(1, interpreter.eval("cos(3-3)"), 1e-15);
		assertEquals(1, interpreter.eval("cosh(0^200)"), 1e-15);
		assertEquals(0, interpreter.eval("cotan(pi/2)"), 1e-15);
		assertEquals(0, interpreter.eval("cot(pi/2)"), 1e-15);
		assertEquals(Math.E * Math.E, interpreter.eval("exp(2)"), 1e-15);
		assertEquals(2, interpreter.eval("floor(3.0 - 0.3)"), 1e-15);
		assertEquals(0.7, interpreter.eval("frac(3-0.3)"), 1e-15);
		assertEquals(2, interpreter.eval("if(1>2;1;2)"), 1e-15);
		assertEquals(2, interpreter.eval("iff(1>2;1;2)"), 1e-15);
		assertEquals(2, interpreter.eval("iif(1>2;1;2)"), 1e-15);
		assertEquals(16, interpreter.eval("ipower(4;2)"), 1e-15);
		assertEquals(2, interpreter.eval("ln(e*e)"), 1e-15);
		assertEquals(3, interpreter.eval("lg(10*10*10)"), 1e-15);
		assertEquals(3, interpreter.eval("log(100 * 10)"), 1e-15);
		assertEquals(12, interpreter.eval("max(8;5;1;8;12)"), 1e-15);
		assertEquals(1, interpreter.eval("min(8;5;1;8;12)"), 1e-15);
		assertEquals(0, interpreter.eval("if(not(4>3);1;0)"), 1e-15);
		assertEquals(1, interpreter.eval("if(or(1>2;3>4;5==5);1;0)"), 1e-15);
		assertEquals(2, interpreter.eval("power(4;0.5)"), 1e-15);
		assertEquals(3, interpreter.eval("pow(9;0.5)"), 1e-15);
		assertEquals(1, interpreter.eval("round(0.5)"), 1e-15);
		assertEquals(0, interpreter.eval("sin(2*PI)"), 1e-15);
		assertEquals(0, interpreter.eval("sinh(4-4)"), 1e-15);
		assertEquals(9, interpreter.eval("sqr(3)"), 1e-15);
		assertEquals(3, interpreter.eval("sqrt(9)"), 1e-15);
		assertEquals(1, interpreter.eval("tan(pi/4)"), 1e-15);
		assertEquals(0, interpreter.eval("tanh(0)"), 1e-15);
		assertEquals(2, interpreter.eval("trunc(2.7)"), 1e-15);
		assertEquals(2, interpreter.eval("int(2.7)"), 1e-15);
	}

}
