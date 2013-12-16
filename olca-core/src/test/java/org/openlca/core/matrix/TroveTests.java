package org.openlca.core.matrix;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.model.AllocationMethod;

public class TroveTests {

	@Test
	public void testConstants() {
		Assert.assertEquals(0L, Constants.DEFAULT_LONG_NO_ENTRY_VALUE);
		Assert.assertEquals(0, Constants.DEFAULT_INT_NO_ENTRY_VALUE);
		Assert.assertEquals(0d, Constants.DEFAULT_DOUBLE_NO_ENTRY_VALUE, 1e-16);
	}

	@Test
	public void testChangeNoEntryValues() {
		//@formatter:off
		TLongIntHashMap map = new TLongIntHashMap(
				Constants.DEFAULT_CAPACITY, 
				Constants.DEFAULT_LOAD_FACTOR, 
				-1L, // no-key value 
				-1 // no entry value
		);
		//@formatter:on

		map.put(4, 2);
		Assert.assertEquals(-1L, map.getNoEntryKey());
		Assert.assertEquals(-1, map.getNoEntryValue());
		Assert.assertEquals(-1, map.get(5L));
		Assert.assertEquals(2, map.get(4L));
		Assert.assertTrue(map.contains(4));
		Assert.assertFalse(map.contains(5));
	}

	@Test
	public void testObjectMapNull() {
		TLongObjectHashMap<AllocationMethod> map = new TLongObjectHashMap<>();
		Assert.assertNull(map.get(4));
		map.put(203040L, AllocationMethod.CAUSAL);
		Assert.assertEquals(AllocationMethod.CAUSAL, map.get(203040L));
	}

}
