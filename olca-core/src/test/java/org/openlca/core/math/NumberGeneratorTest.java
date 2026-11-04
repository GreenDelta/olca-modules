package org.openlca.core.math;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;
import org.openlca.core.math.rand.NumberGenerator;

public class NumberGeneratorTest {

	@Test
	public void testDiscrete() {
		var genDiscrete = NumberGenerator.normal(5, 0);
		for (int i = 0; i < 100; i++)
			assertEquals(5.0, genDiscrete.next(), 1e-16);
	}

	@Test
	public void testNormal() {
		var gen = NumberGenerator.normal(5, 1);
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		for (int i = 0; i < 100; i++) {
			double next = gen.next();
			min = Math.min(min, next);
			max = Math.max(max, next);
			assertInInterval(gen.next(), -100, 100);
		}
		assertTrue(max > min);
	}

	@Test
	public void testLogNormal() {
		var gen = NumberGenerator.logNormal(5, 1.1);
		for (int i = 0; i < 100; i++) {
			assertInInterval(gen.next(), 0, 100);
		}
		gen = NumberGenerator.logNormal(5, 1);
		for (int i = 0; i < 100; i++) {
			assertEquals(5.0, gen.next(), 1e-5);
		}

		var nums = List.of(
			-2.0, -1.0, -0.5, -0.1, -0.01, 0, 0.01, 0.1, 0.5, 1.0, 2.0
		);
		var rand = ThreadLocalRandom.current();
		for (var d : nums) {
			int idx = rand.nextInt(0, nums.size());
			var gmean = nums.get(idx).doubleValue();
			var ng = NumberGenerator.logNormal(gmean, d.doubleValue());
			for (int i = 0; i < 10; i++) {
				assertTrue(Double.isFinite(ng.next()));
			}
		}
	}

	@Test
	public void testUniform() {
		var gen = NumberGenerator.uniform(1, 5);
		for (int i = 0; i < 100; i++)
			assertInInterval(gen.next(), 1, 5);
	}

	@Test
	public void testTriangular() {
		var gen = NumberGenerator.triangular(1, 4, 5);
		for (int i = 0; i < 100; i++)
			assertInInterval(gen.next(), 1, 5);
		var genDiscrete = NumberGenerator.triangular(5, 5, 5);
		for (int i = 0; i < 100; i++)
			assertEquals(5.0, genDiscrete.next(), 1e-16);
	}

	private void assertInInterval(double val, double lower, double upper) {
		assertTrue(val >= lower);
		assertTrue(val <= upper);
	}
}
