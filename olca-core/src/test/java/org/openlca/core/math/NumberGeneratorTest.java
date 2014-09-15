package org.openlca.core.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NumberGeneratorTest {

	@Test
	public void testNormal() {
		NumberGenerator gen = NumberGenerator.normal(5, 1);
		for (int i = 0; i < 100; i++)
			assertInInterval(gen.next(), -100, 100);
		NumberGenerator genDiscrete = NumberGenerator.normal(5, 0);
		for (int i = 0; i < 100; i++)
			assertEquals(5.0, genDiscrete.next(), 1e-16);
	}

	@Test
	public void testLogNormal() {
		// TODO: test log-normal
		NumberGenerator gen = NumberGenerator.logNormal(5, 1);
	}

	@Test
	public void testUniform() {
		NumberGenerator gen = NumberGenerator.uniform(1, 5);
		for (int i = 0; i < 100; i++)
			assertInInterval(gen.next(), 1, 5);
		NumberGenerator genDiscrete = NumberGenerator.uniform(5, 5);
		for (int i = 0; i < 100; i++)
			assertEquals(5.0, genDiscrete.next(), 1e-16);
	}

	@Test
	public void testTriangular() {
		NumberGenerator gen = NumberGenerator.triangular(1, 4, 5);
		for (int i = 0; i < 100; i++)
			assertInInterval(gen.next(), 1, 5);
		NumberGenerator genDiscrete = NumberGenerator.triangular(5, 5, 5);
		for (int i = 0; i < 100; i++)
			assertEquals(5.0, genDiscrete.next(), 1e-16);
	}

	private void assertInInterval(double val, double lower, double upper) {
		assertTrue(val >= lower);
		assertTrue(val <= upper);
	}
}
