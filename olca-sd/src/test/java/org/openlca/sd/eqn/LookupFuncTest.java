package org.openlca.sd.eqn;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.sd.model.LookupFunc;
import org.openlca.sd.model.LookupFunc.Type;

/// Tests the lookup function with `y = 1.5 * x - 2` and `x in [3, 7]`:
/// ```
///   i | x    | y
///   0 | 3.00 | 2.50
///   1 | 3.50 | 3.25
///   2 | 4.00 | 4.00
///   3 | 4.50 | 4.75
///   4 | 5.00 | 5.50
///   5 | 5.50 | 6.25
///   6 | 6.00 | 7.00
///   7 | 6.50 | 7.75
///   8 | 7.00 | 8.50
/// ```
public class LookupFuncTest {

	private final double[] xs = {
		3.0, 3.5, 4.0, 4.5, 5.0, 5.5, 6.0, 6.5, 7.0
	};

	private final double[] ys = {
		2.5, 3.25, 4.0, 4.75, 5.5, 6.25, 7.0, 7.75, 8.5
	};

	@Test
	public void testContinuous() {
		var fn = new LookupFunc(Type.CONTINUOUS, 3, 7, ys);
		checkDirectMatches(fn);
		assertEquals(2.5, fn.get(1.0), 1e-16);
		assertEquals(3.625, fn.get(3.75), 1e-16);
		assertEquals(5.875, fn.get(5.25), 1e-16);
		assertEquals(8.125, fn.get(6.75), 1e-16);
		assertEquals(8.5, fn.get(8), 1e-16);
	}

	@Test
	public void testExtrapolate() {
		var fn = new LookupFunc(Type.EXTRAPOLATE, 3, 7, ys);
		checkDirectMatches(fn);
		assertEquals(-0.5, fn.get(1.0), 1e-16);
		assertEquals(3.625, fn.get(3.75), 1e-16);
		assertEquals(5.875, fn.get(5.25), 1e-16);
		assertEquals(8.125, fn.get(6.75), 1e-16);
		assertEquals(10.0, fn.get(8), 1e-16);
	}

	@Test
	public void testDiscrete() {
		var fn = new LookupFunc(Type.DISCRETE, 3, 7, ys);
		checkDirectMatches(fn);
		assertEquals(2.5, fn.get(1.0), 1e-16);
		assertEquals(3.25, fn.get(3.75), 1e-16);
		assertEquals(5.5, fn.get(5.25), 1e-16);
		assertEquals(7.75, fn.get(6.75), 1e-16);
		assertEquals(8.5, fn.get(8), 1e-16);
	}

	private void checkDirectMatches(LookupFunc fn) {
		for (int i = 0; i < xs.length; i++) {
			assertEquals(ys[i], fn.get(xs[i]), 1e-16);
		}
	}
}
