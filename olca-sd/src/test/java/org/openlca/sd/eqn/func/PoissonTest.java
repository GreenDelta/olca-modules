package org.openlca.sd.eqn.func;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.sd.model.cells.Cell;

public class PoissonTest {

	@Test
	public void testPoissonDist() {
		var func = new Poisson();
		for (int i = 0; i < 10; i++) {
			var value = func.apply(List.of(Cell.of(i + 1.0)))
				.orElseThrow()
				.asNum();
			Assert.assertTrue(value >= 0);
		}
	}
}
