package org.openlca.core.matrix.io.npy;

import org.junit.Assert;
import org.junit.Test;

public class HeaderTest {

	@Test
	public void testParseString() {
		String s = "{'descr': '<i4', 'fortran_order': False, 'shape': (2,), }";
		Header h = Header.parse(s);
		Assert.assertEquals("<i4", h.dtype);
		Assert.assertArrayEquals(new int[]{2}, h.shape);
		Assert.assertFalse(h.fortranOrder);
		Assert.assertEquals(s, h.toString());
	}

}
