package org.openlca.core.library.reader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.file.Files;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.library.LibMatrix;
import org.openlca.core.library.Library;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.io.NpyMatrix;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.util.Dirs;

public class SolvingLibReaderTest {

	private static SolvingLibReader r;

	@BeforeClass
	public static void setup() throws Exception {
		var dir = Files.createTempDirectory("_olca_").toFile();
		var techMatrix = DenseMatrix.of(new double[][]{
				{1.0, -0.5},
				{1.0, -1.0}
		});
		var enviMatrix = DenseMatrix.of(new double[][]{
				{2.0, 4.0}
		});
		NpyMatrix.write(dir, "A", techMatrix);
		NpyMatrix.write(dir, "B", enviMatrix);

		var lib = Library.of(dir);
		r = SolvingLibReader.of(
				DirectLibReader.of(lib, Tests.getDb()),
				new JavaSolver());
	}

	@AfterClass
	public static void cleanup() {
		r.dispose();
		Dirs.delete(r.library().folder());
	}

	@Test
	public void testSolutions() {
		r.reset();
		assertCacheEmpty(LibMatrix.INV);
		assertArrayEquals(a(2.0, 2.0), r.columnOf(LibMatrix.INV, 0), 1e-9);
		assertArrayEquals(a(-1.0, -2.0), r.columnOf(LibMatrix.INV, 1), 1e-9);
		assertCachePresent(LibMatrix.INV);
	}

	@Test
	public void testFullInverse() {
		r.reset();
		assertCacheEmpty(LibMatrix.INV);
		var inv = r.matrixOf(LibMatrix.INV);
		assertNotNull(inv);
		assertArrayEquals(a(2.0, 2.0), inv.getColumn(0), 1e-9);
		assertArrayEquals(a(-1.0, -2.0), inv.getColumn(1), 1e-9);
		assertCachePresent(LibMatrix.INV);
	}

	@Test
	public void testIntensities() {
		r.reset();
		assertCacheEmpty(LibMatrix.M);
		assertArrayEquals(a(12.0), r.columnOf(LibMatrix.M, 0), 1e-9);
		assertArrayEquals(a(-10.0), r.columnOf(LibMatrix.M, 1), 1e-9);
		assertCachePresent(LibMatrix.M);
	}

	@Test
	public void testFullIntensities() {
		r.reset();
		assertCacheEmpty(LibMatrix.M);
		var m = r.matrixOf(LibMatrix.M);
		assertArrayEquals(a(12.0), m.getColumn(0), 1e-9);
		assertArrayEquals(a(-10.0), m.getColumn(1), 1e-9);
		assertCachePresent(LibMatrix.M);
	}

	@Test
	public void testMatrixReading() {
		assertArrayEquals(a(1.0, 1.0), r.columnOf(LibMatrix.A, 0), 1e-9);
		assertArrayEquals(a(-0.5, -1.0), r.columnOf(LibMatrix.A, 1), 1e-9);
		assertCachePresent(LibMatrix.A);
		assertArrayEquals(a(2.0), r.columnOf(LibMatrix.B, 0), 1e-9);
		assertArrayEquals(a(4.0), r.columnOf(LibMatrix.B, 1), 1e-9);
		assertCachePresent(LibMatrix.B);
	}

	private void assertCachePresent(LibMatrix m) {
		var cache = r.cache();
		assertNotNull(cache.columnOf(m, 0));
		assertNotNull(cache.columnOf(m, 1));
	}

	private void assertCacheEmpty(LibMatrix m) {
		var cache = r.cache();
		assertNull(cache.columnOf(m, 0));
		assertNull(cache.columnOf(m, 1));
	}

	private double[] a(double... xs) {
		return xs;
	}
}

