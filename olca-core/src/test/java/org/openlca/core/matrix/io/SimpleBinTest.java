package org.openlca.core.matrix.io;

import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.Matrix;

public class SimpleBinTest {

	@Test
	public void testMatrixIO() throws Exception {
		var f = Files.createTempFile("olcamat-", ".bin").toFile();
		Matrix m = new DenseMatrix(2, 3);
		m.setValues(new double[][] {
				{ 1, 2, 3 },
				{ 4, 5, 6 }
		});
		SimpleBin.write(m, f);
		m = SimpleBin.read(f);
		Assert.assertArrayEquals(new double[] { 1, 4 }, m.getColumn(0), 1e-16);
		Assert.assertArrayEquals(new double[] { 2, 5 }, m.getColumn(1), 1e-16);
		Assert.assertArrayEquals(new double[] { 3, 6 }, m.getColumn(2), 1e-16);

		// the memory mappings are released when SimpleBin returns, so the
		// file can be deleted directly, also on Windows (see issue #17)
		Files.delete(f.toPath());
	}

	@Test
	public void testOverwriteLargerFile() throws Exception {
		var f = Files.createTempFile("olcamat-", ".bin").toFile();
		var big = new DenseMatrix(10, 10);
		SimpleBin.write(big, f);

		// writing a smaller matrix must truncate the file
		var small = new DenseMatrix(1, 2);
		small.set(0, 0, 42);
		small.set(0, 1, 21);
		SimpleBin.write(small, f);
		Assert.assertEquals(8 + 2 * 8, f.length());

		var m = SimpleBin.read(f);
		Assert.assertEquals(1, m.rows());
		Assert.assertEquals(2, m.columns());
		Assert.assertEquals(42, m.get(0, 0), 1e-16);
		Assert.assertEquals(21, m.get(0, 1), 1e-16);
		Files.delete(f.toPath());
	}
}
