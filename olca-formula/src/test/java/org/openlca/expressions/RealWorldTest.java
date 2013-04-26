package org.openlca.expressions;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.expressions.Interpreter;
import org.openlca.expressions.Variable;

public class RealWorldTest {

	private Interpreter interpreter = new Interpreter();

	@Test
	public void greatCircle() throws Exception {
		// great circle distance between Berlin and Brussels
		interpreter.bind(new Variable("b_a", "52.52 * pi/180"));
		interpreter.bind(new Variable("l_a", "13.41 * pi/180"));
		interpreter.bind(new Variable("b_b", "50.85 * pi/180"));
		interpreter.bind(new Variable("l_b", "4.35 * pi/180"));
		interpreter.bind(new Variable("re", "6378"));
		interpreter
				.bind(new Variable("d",
						"re * arccos(sin(b_a) * sin(b_b) + cos(b_a) * cos(b_b) * cos(l_b-l_a))"));
		double val = interpreter.eval("d");
		assertEquals(651.81, val, 0.01);
	}

	@Test
	public void boxMuller() throws Exception {
		// generate normal distributed random numbers from
		// uniform distributed random numbers
		interpreter.bind(new Variable("z1",
				"sqrt(-2 * ln(random())) * sin(2 * pi * random())"));
		interpreter.bind(new Variable("z2",
				"sqrt(-2 * ln(random())) * sin(2 * pi * random())"));
		double z1 = interpreter.eval("z1");
		double z2 = interpreter.eval("z2");
		assertTrue(z1 != z2);
	}

	@Test
	public void bandFilter() throws Exception {
		interpreter.bind(new Variable("R_p", "1800"));
		interpreter.bind(new Variable("R_l", "1.2"));
		interpreter.bind(new Variable("g", "9.81"));
		interpreter.bind(new Variable("d_p", "10^-3"));
		interpreter.bind(new Variable("n_l", "1.8*10^-5"));
		interpreter.bind(new Variable("Ar",
				"(R_p-R_l) * R_l * g * d_p^3 / n_l^2"));
		interpreter
				.bind(new Variable("Re_p",
						"42.9*(1-0.4) * (sqrt(1 + 3.11*10^-4 * (0.4^3 / (1-0.4)^2) * Ar) - 1)"));
		interpreter.bind(new Variable("v_min", "1 * Re_p * n_l / (d_p * R_l)"));
		assertEquals(0.44, interpreter.eval("v_min"), 0.01);
	}

}
