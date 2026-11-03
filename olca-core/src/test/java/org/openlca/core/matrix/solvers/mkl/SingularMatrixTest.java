package org.openlca.core.matrix.solvers.mkl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.linear.SingularMatrixException;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.MatrixReader;

/// Singular matrices must result in a [SingularMatrixException] and
/// never crash the JVM. These matrices crashed the former JNI wrapper because
/// PARDISO perturbed pivots and then ran an iterative refinement without
/// access to the matrix.
public class SingularMatrixTest {

	private final MKLSolver solver = new MKLSolver();

	@BeforeClass
	public static void setup() {
		Assume.assumeTrue(MKL.loadFromDefault());
	}

	/// Column 2 is equal to column 0.
	private static double[][] duplicateColumn() {
		return new double[][]{
			{1, 0, 1},
			{2, 1, 2},
			{1, 1, 1}};
	}

	/// Column 2 is empty.
	private static double[][] zeroColumn() {
		return new double[][]{
			{1, 0, 0},
			{0, 1, 0},
			{0, 0, 0}};
	}

	/// Column 2 is the sum of columns 0 and 1.
	private static double[][] linearCombination() {
		return new double[][]{
			{1, -0.5, 0.5},
			{-1, 1, 0},
			{0, -0.5, -0.5}};
	}

	private static double[][][] singularMatrices() {
		return new double[][][]{
			duplicateColumn(), zeroColumn(), linearCombination()};
	}

	@Test
	public void testSparseSolve() {
		for (var m : singularMatrices()) {
			assertThrows(SingularMatrixException.class,
				() -> solver.solve(HashPointMatrix.of(m), 0, 1.0));
		}
	}

	@Test
	public void testSparseSolveNative() {
		var csc = HashPointMatrix.of(duplicateColumn()).pack();
		var x = new double[3];
		int info = MKL.solveSparse(3, csc.values, csc.rowIndices,
			csc.columnPointers, new double[]{1, 0, 0}, x);
		assertNotEquals(0, info);
	}

	@Test
	public void testSparseFactorization() {
		for (var m : singularMatrices()) {
			assertThrows(SingularMatrixException.class,
				() -> solver.factorize(HashPointMatrix.of(m)));
		}
	}

	@Test
	public void testDenseSolve() {
		for (var m : singularMatrices()) {
			assertThrows(SingularMatrixException.class,
				() -> solver.solve(DenseMatrix.of(m), 0, 1.0));
		}
	}

	@Test
	public void testDenseFactorization() {
		for (var m : singularMatrices()) {
			assertThrows(SingularMatrixException.class,
				() -> solver.factorize(DenseMatrix.of(m)));
		}
	}

	@Test
	public void testDenseInvert() {
		for (var m : singularMatrices()) {
			assertThrows(SingularMatrixException.class,
				() -> solver.invert(DenseMatrix.of(m)));
		}
	}

	@Test
	public void testDisposeTwice() {
		for (var m : regularMatrices()) {
			var f = solver.factorize(m);
			f.dispose();
			f.dispose();
			assertTrue(f.isDisposed());
			assertThrows(IllegalStateException.class,
				() -> f.solve(new double[]{1, 0}));
		}
	}

	@Test
	public void testMultipleSolves() {
		for (var m : regularMatrices()) {
			var f = solver.factorize(m);
			for (int i = 0; i < 10; i++) {
				assertArrayEquals(new double[]{2, 2},
					f.solve(new double[]{1, 0}), 1e-15);
				assertArrayEquals(new double[]{1, 2},
					f.solve(new double[]{0, 1}), 1e-15);
			}
			f.dispose();
		}
	}

	private static MatrixReader[] regularMatrices() {
		double[][] data = {
			{1, -0.5},
			{-1, 1.0}};
		return new MatrixReader[]{
			HashPointMatrix.of(data), DenseMatrix.of(data)};
	}
}
