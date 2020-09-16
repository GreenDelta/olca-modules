package org.openlca.core.matrix.io.npy;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.matrix.format.DenseMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;

public class NpyTest {

	private File npy;
	private DenseMatrix matrix;

	@Before
	public void setup() throws Exception {
		matrix = new DenseMatrix(2, 3);
		matrix.setValues(new double[][]{
				{1., 2., 3.},
				{4., 5., 6.}
		});
		npy = Files.createTempFile("__olca_npy_test_", ".npy").toFile();
		Npy.save(npy, matrix);
	}

	@After
	public void tearDown() {
		if (!npy.delete()) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.warn("failed to delete test file {}", npy);
			npy.deleteOnExit();
		}
	}

	@Test
	public void testLoad() {
		DenseMatrix copy = Npy.load(npy);
		Assert.assertEquals(matrix.rows, copy.rows());
		Assert.assertEquals(matrix.columns, copy.columns());
		Assert.assertArrayEquals(matrix.data, copy.data, 1e-10);
	}

	@Test
	public void testLoadColumn() {
		for (int j = 0; j < matrix.columns; j++) {
			Assert.assertArrayEquals(
					matrix.getColumn(j), Npy.loadColumn(npy, j), 1e-10);
		}
	}

	@Test
	public void testLoadDiagonal() {
		var diag = Npy.loadDiagonal(npy);
		Assert.assertArrayEquals(
				new double[]{1, 5}, diag, 1e-10);

	}
}
