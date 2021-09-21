package org.openlca.core.matrix.io.npy;

import java.io.File;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.matrix.format.DenseByteMatrix;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.io.NpyMatrix;
import org.openlca.npy.Array2d;
import org.openlca.util.Dirs;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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
		var dir = Files.createTempDirectory("_olca_tests").toFile();
		npy = NpyMatrix.write(dir, "M", matrix);
	}

	@After
	public void tearDown() {
		Dirs.delete(npy.getParentFile());
	}

	@Test
	public void testLoad() {
		DenseMatrix copy = (DenseMatrix) NpyMatrix.read(npy);
		assertEquals(matrix.rows, copy.rows());
		assertEquals(matrix.columns, copy.columns());
		assertArrayEquals(matrix.data, copy.data, 1e-10);
	}

	@Test
	public void testLoadColumn() {
		for (int j = 0; j < matrix.columns; j++) {
			var col = Array2d.readColumn(npy, j)
				.asDoubleArray()
				.data();
			assertArrayEquals(matrix.getColumn(j), col, 1e-10);
		}
	}

	@Test
	public void testLoadDiagonal() {
		var diag = Array2d.readDiag(npy)
			.asDoubleArray()
			.data();
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

		var dir = Files.createTempDirectory("_olca_tests").toFile();
		NpyMatrix.write(dir, "M", m);
		var copy = NpyMatrix.readBytes(dir, "M")
			.map(DenseByteMatrix.class::cast)
			.orElseThrow();
		assertArrayEquals(m.data, copy.data);
		assertEquals(m.rows, copy.rows);
		assertEquals(m.columns, copy.columns);
		Dirs.delete(dir);

	}

}
