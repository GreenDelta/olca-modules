package org.openlca.core.matrix.io.npy;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.matrix.format.DenseMatrix;

import java.io.File;
import java.nio.file.Files;

public class NpyTest {

	@Test
	public void writeReadDense() throws Exception {
		DenseMatrix m = new DenseMatrix(2, 3);
		m.setValues(new double[][]{
				{1., 2., 3.},
				{4., 5., 6.}
		});
		File file = Files.createTempFile("__olca_npy_test_", ".npy").toFile();
		Npy.save(file, m);
		DenseMatrix copy = (DenseMatrix)Npy.load(file);
		Assert.assertEquals(m.rows(), copy.rows());
		Assert.assertEquals(m.columns(), copy.columns());
		Assert.assertArrayEquals(m.getData(), copy.getData(), 1e-10);
		file.delete();
	}
}
