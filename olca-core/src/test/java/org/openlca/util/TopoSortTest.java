package org.openlca.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.openlca.core.matrix.index.LongPair;

public class TopoSortTest {

	@Test
	public void testEmpty() {
		List<Long> ordered = TopoSort.of(
				Collections.emptyList());
		assertTrue(ordered.isEmpty());
	}

	@Test
	public void testSimple() {
		List<Long> ordered = TopoSort.of(Arrays.asList(
				LongPair.of(3, 4),
				LongPair.of(2, 3),
				LongPair.of(1, 2)));
		assertArrayEquals(
				new Long[] { 1L, 2L, 3L, 4L },
				ordered.toArray(new Long[4]));
	}

	@Test
	public void testCycle() {
		List<Long> ordered = TopoSort.of(Arrays.asList(
				LongPair.of(4, 3),
				LongPair.of(3, 4),
				LongPair.of(2, 3),
				LongPair.of(1, 2)));
		assertNull(ordered);
	}

}
