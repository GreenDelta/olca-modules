package org.openlca.core.matrix.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlca.core.model.descriptors.ImpactDescriptor;

public class ImpactIndexTest {

	@Test
	public void testIndex() {
		var index = new ImpactIndex();
		for (int i = 1; i < 11; i++) {
			var d = new ImpactDescriptor();
			d.id = i;
			index.add(d);
		}
		assertEquals(10, index.size());

		assertEquals(10, index.size());
		for (int i = 1; i < 11; i++) {
			assertTrue(index.contains(i));
			assertEquals(i - 1, index.of(i));
			ImpactDescriptor d = index.at(i - 1);
			assertEquals(i - 1, index.of(d));
			assertTrue(index.contains(d));
			assertEquals(i, d.id);
		}
	}
}
