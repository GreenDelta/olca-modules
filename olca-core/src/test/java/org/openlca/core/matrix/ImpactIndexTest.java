package org.openlca.core.matrix;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public class ImpactIndexTest {

	@Test
	public void testIndex() {
		ImpactIndex index = new ImpactIndex();
		assertArrayEquals(new long[] {}, index.ids());
		for (int i = 1; i < 11; i++) {
			var d = new ImpactCategoryDescriptor();
			d.id = i;
			index.put(d);
		}
		long[] ids = index.ids();
		assertEquals(10, ids.length);

		assertEquals(10, index.size());
		for (int i = 1; i < 11; i++) {
			assertTrue(index.contains(i));
			assertEquals(i - 1, index.of(i));
			ImpactCategoryDescriptor d = index.at(i - 1);
			assertEquals(i - 1, index.of(d));
			assertTrue(index.contains(d));
			assertEquals(i, d.id);
		}
	}
}
