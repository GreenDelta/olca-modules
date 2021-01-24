package org.openlca.core.matrix.io.npy;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.matrix.format.DenseByteMatrix;
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
		assertEquals(matrix.rows, copy.rows());
		assertEquals(matrix.columns, copy.columns());
		assertArrayEquals(matrix.data, copy.data, 1e-10);
	}

	@Test
	public void testLoadColumn() {
		for (int j = 0; j < matrix.columns; j++) {
			var col = Npy.loadColumn(npy, j);
			assertArrayEquals(matrix.getColumn(j), col, 1e-10);
		}
	}

	@Test
	public void testLoadDiagonal() {
		var diag = Npy.loadDiagonal(npy);
		assertArrayEquals(new double[]{1, 5}, diag, 1e-10);

	}

	@Test
	public void testByteMatrix() throws Exception {
		var m = new DenseByteMatrix(2, 3);
		for (int col = 0; col < 3; col++) {
			for (int row = 0; row < 2; row++) {
				m.set(row, col, (byte) ((row + 1) + col * 2));
			}
		}
		var f = Files.createTempFile("_olca_test", ".npy").toFile();
		Npy.save(f, m);
		var copy = Npy.loadByteMatrix(f);
		assertArrayEquals(m.data, copy.data);
		assertEquals(m.rows, copy.rows);
		assertEquals(m.columns, copy.columns);
	}

}
