package org.openlca.sd.eqn.func;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.sd.model.cells.Cell;

public class LogNormalTest {

	@Test
	public void testLogNormalDist() {
		var func = new LogNormal();
		for (int i = 1; i <= 10; i++) {
			var value = func.apply(List.of(Cell.of(i * 1.0), Cell.of(1.0)))
				.orElseThrow()
				.asNum();
			Assert.assertTrue("Value should be non-negative", value >= 0);
		}
	}

	@Test
	public void testLogNormalInsufficientArgs() {
		var func = new LogNormal();
		var result = func.apply(List.of(Cell.of(1.0)));
		Assert.assertTrue("Should return error for insufficient arguments", result.isError());
	}

	@Test
	public void testLogNormalNoArgs() {
		var func = new LogNormal();
		var result = func.apply(List.of());
		Assert.assertTrue("Should return error for no arguments", result.isError());
	}

	@Test
	public void testLogNormalNonNumericArgs() {
		var func = new LogNormal();
		var result = func.apply(List.of(Cell.of("not a number"), Cell.of(1.0)));
		Assert.assertTrue("Should return error for non-numeric first argument", result.isError());

		result = func.apply(List.of(Cell.of(1.0), Cell.of("not a number")));
		Assert.assertTrue("Should return error for non-numeric second argument", result.isError());
	}
}
