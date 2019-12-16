package org.openlca.core.matrix.io.npy;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.matrix.format.DenseMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		DenseMatrix copy = Npy.load(file);
		Assert.assertEquals(m.rows(), copy.rows());
		Assert.assertEquals(m.columns(), copy.columns());
		Assert.assertArrayEquals(m.data, copy.data, 1e-10);
		if (!file.delete()) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.warn("failed to delete test file {}", file);
			file.deleteOnExit();
		}
	}
}
