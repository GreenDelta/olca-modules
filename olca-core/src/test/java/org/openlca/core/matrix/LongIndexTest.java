package org.openlca.core.matrix;

import org.junit.Assert;
import org.junit.Test;

public class LongIndexTest {

	@Test
	public void testLongIndex() {
		LongIndex idx = new LongIndex();
		Assert.assertTrue(idx.isEmpty());
		for (long i = 1; i <= 1000; i++) {
			Assert.assertEquals(i - 1, idx.size());
			Assert.assertFalse(idx.contains(i));
			idx.put(i);
			Assert.assertTrue(idx.contains(i));
		}
		Assert.assertTrue(idx.size() == 1000);
		long[] keys = idx.getKeys();
		for (long i = 0; i < keys.length; i++) {
			Assert.assertEquals(i + 1, keys[(int) i]);
		}
	}

	@Test
	public void testGetDefault() {
		LongIndex idx = new LongIndex();
		idx.put(23L);
		Assert.assertEquals(1, idx.size());
		Assert.assertEquals(-1, idx.getIndex(42));
	}

}
