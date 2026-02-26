package org.openlca.sd.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.sd.model.Dimension;
import org.openlca.sd.model.Tensor;

public class TensorsTest {

	@Test
	public void testAddresses() {
		var rows = Dimension.of("rows", "a", "b", "c");
		var cols = Dimension.of("cols", "d", "e");
		var t = Tensor.of(rows, cols);
		var ax = Tensors.addressesOf(t);

		assertEquals(6, ax.size());
		var expecteds = new String[] {
				"a, d", "b, d", "c, d",
				"a, e", "b, e", "c, e"
		};
		var actuals = new String[6];
		for (int i = 0; i < ax.size(); i++) {
			var s = ax.get(i)
					.stream()
					.map(sub -> sub.toString())
					.toList();
			actuals[i] = String.join(", ", s);
		}
		assertArrayEquals(expecteds, actuals);
	}
}
