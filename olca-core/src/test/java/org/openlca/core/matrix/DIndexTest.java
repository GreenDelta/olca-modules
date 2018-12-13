package org.openlca.core.matrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public class DIndexTest {

	@Test
	public void testIndex() {
		DIndex<ImpactCategoryDescriptor> index = new DIndex<>();
		for (int i = 1; i < 11; i++) {
			ImpactCategoryDescriptor d = new ImpactCategoryDescriptor();
			d.setId(i);
			index.put(d);
		}

		assertEquals(10, index.size());
		for (int i = 1; i < 11; i++) {
			assertTrue(index.contains(i));
			assertEquals(i - 1, index.of(i));
			ImpactCategoryDescriptor d = index.at(i - 1);
			assertEquals(i - 1, index.of(d));
			assertTrue(index.contains(d));
			assertEquals((long) i, d.getId());
		}
	}

}
