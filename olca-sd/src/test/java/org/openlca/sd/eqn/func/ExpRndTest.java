package org.openlca.sd.eqn.func;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.openlca.sd.model.cells.Cell;

public class ExpRndTest {

	@Test
	public void testExpRndDist() {
		var func = new ExpRnd();
		for (int i = 1; i <= 10; i++) {
			var value = func.apply(List.of(Cell.of(i * 1.0)))
				.orElseThrow()
				.asNum();
			assertTrue("Value should be non-negative", value >= 0);
		}
	}

	@Test
	public void testExpRndNoArgs() {
		var func = new ExpRnd();
		var res = func.apply(List.of());
		assertTrue("Should return error for no arguments", res.isError());
	}

	@Test
	public void testExpRndNonNumericArg() {
		var func = new ExpRnd();
		var res = func.apply(List.of(Cell.of("not a number")));
		assertTrue("Should return error for non-numeric argument", res.isError());
	}
}
