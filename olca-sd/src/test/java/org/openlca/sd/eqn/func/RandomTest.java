package org.openlca.sd.eqn.func;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.sd.model.cells.Cell;

public class RandomTest {

	@Test
	public void testRandomInRange() {
		var func = new Random();
		for (int i = 0; i < 100; i++) {
			var value = func.apply(List.of(Cell.of(1.0), Cell.of(10.0)))
				.orElseThrow()
				.asNum();
			Assert.assertTrue("Value should be >= 1.0", value >= 1.0);
			Assert.assertTrue("Value should be <= 10.0", value <= 10.0);
		}
	}

	@Test
	public void testRandomNegativeRange() {
		var func = new Random();
		for (int i = 0; i < 100; i++) {
			var value = func.apply(List.of(Cell.of(-10.0), Cell.of(-1.0)))
				.orElseThrow()
				.asNum();
			Assert.assertTrue("Value should be >= -10.0", value >= -10.0);
			Assert.assertTrue("Value should be < -1.0", value < -1.0);
		}
	}

	@Test
	public void testRandomInsufficientArgs() {
		var func = new Random();
		var result = func.apply(List.of(Cell.of(1.0)));
		Assert.assertTrue("Should return error for insufficient arguments", result.isError());
	}

	@Test
	public void testRandomNoArgs() {
		var func = new Random();
		var result = func.apply(List.of());
		Assert.assertTrue("Should return error for no arguments", result.isError());
	}

	@Test
	public void testRandomInvalidRange() {
		var func = new Random();
		var result = func.apply(List.of(Cell.of(10.0), Cell.of(1.0)));
		Assert.assertTrue("Should return error when min >= max", result.isError());
	}

	@Test
	public void testRandomNonNumericArgs() {
		var func = new Random();
		var result = func.apply(List.of(Cell.of("not a number"), Cell.of(1.0)));
		Assert.assertTrue("Should return error for non-numeric first argument", result.isError());

		result = func.apply(List.of(Cell.of(1.0), Cell.of("not a number")));
		Assert.assertTrue("Should return error for non-numeric second argument", result.isError());
	}
}
