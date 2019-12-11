package org.openlca.core.matrix.io.npy;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.matrix.format.DenseMatrix;

import java.io.File;
import java.nio.ByteOrder;
import java.nio.file.Files;

public class HeaderTest {

	@Test
	public void testParseString() {
		String s = "{'descr': '<i4', 'fortran_order': False, 'shape': (2,), }";
		Header h = Header.read(s);
		Assert.assertEquals("<i4", h.dtype);
		Assert.assertEquals(DType.Int32, h.getDType());
		Assert.assertEquals(ByteOrder.LITTLE_ENDIAN, h.getByteOrder());
		Assert.assertArrayEquals(new int[]{2}, h.shape);
		Assert.assertFalse(h.fortranOrder);
		Assert.assertEquals(s, h.toString());
	}

	@Test
	public void testFromReadFile() throws Exception {
		DenseMatrix m = new DenseMatrix(2, 3);
		File file = Files.createTempFile("_olca_npy_tests", ".npy").toFile();
		Npy.save(file, m);
		Header h = Header.read(file);
		Header h2 = Header.read(file);
		Assert.assertEquals(h.toString(), h2.toString());
		Assert.assertEquals("<f8", h.dtype);
		Assert.assertEquals(DType.Float64, h.getDType());
		Assert.assertEquals(ByteOrder.LITTLE_ENDIAN, h.getByteOrder());
		Assert.assertTrue(h.fortranOrder);
		Assert.assertArrayEquals(new int[]{2, 3}, h.shape);
		if (!file.delete()) {
			// deleting the file will probably fail on Windows
			// see https://github.com/GreenDelta/olca-modules/issues/17
			file.deleteOnExit();
		}
	}
}
