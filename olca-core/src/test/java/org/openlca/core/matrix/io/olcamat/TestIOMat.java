package org.openlca.core.matrix.io.olcamat;

import java.io.File;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.IMatrix;

public class TestIOMat {

	@Test
	public void testMatrixIO() throws Exception {
		File f = Files.createTempFile("olcamat-", ".bin").toFile();
		IMatrix m = new DenseMatrix(2, 3);
		m.setValues(new double[][] {
				{ 1, 2, 3 },
				{ 4, 5, 6 }
		});
		IOMat.writeMatrix(m, f);
		m = IOMat.readMatrix(f);
		Assert.assertArrayEquals(new double[] { 1, 4 }, m.getColumn(0), 1e-16);
		Assert.assertArrayEquals(new double[] { 2, 5 }, m.getColumn(1), 1e-16);
		Assert.assertArrayEquals(new double[] { 3, 6 }, m.getColumn(2), 1e-16);
		f.delete();
	}

}
