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

		// https://bugs.openjdk.java.net/browse/JDK-6558368
		// SimpleBin does memory mapping; this means that the file cannot
		// be deleted on Windows until the MappedByteBuffer is released
		// via the garbage collector. Files.delete then fails.
		// Files.delete(f.toPath());
		if (!f.delete()) {
			f.deleteOnExit();
		}
	}

}
