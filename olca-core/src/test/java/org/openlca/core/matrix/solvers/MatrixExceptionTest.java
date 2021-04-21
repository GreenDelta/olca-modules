package org.openlca.core.matrix.solvers;

import static org.junit.Assert.*;

import org.apache.commons.math3.linear.NonSquareMatrixException;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.junit.Ignore;
import org.junit.Test;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.JavaMatrix;
import org.openlca.julia.Julia;
import org.openlca.julia.JuliaSolver;

public class MatrixExceptionTest {

	@Test(expected = SingularMatrixException.class)
	public void testJavaSolveSingular() {
		var matrix = JavaMatrix.of(new double[][]{
			{1.0, -2.0},
			{-1.0, 2.0},
		});
		new JavaSolver().solve(matrix, 1, 1.0);
	}

	@Test(expected = NonSquareMatrixException.class)
	public void testJavaSolveNonSquare() {
		var matrix = JavaMatrix.of(new double[][]{
			{1.0, 0.0},
			{-1.0, 2.0},
			{-1.0, 0.0},
		});
		new JavaSolver().solve(matrix, 1, 1.0);
	}

	@Test(expected = NonSquareMatrixException.class)
	public void testNativeSolveNonSquare() {
		var matrix = DenseMatrix.of(new double[][]{
			{1.0, 0.0},
			{-1.0, 2.0},
			{-1.0, 0.0},
		});
		new JuliaSolver().solve(matrix, 1, 1.0);
	}

	@Test(expected = SingularMatrixException.class)
	public void testBlasSolveSingular() {
		assertTrue(Julia.load());
		var matrix = DenseMatrix.of(new double[][]{
			{1.0, -2.0},
			{-1.0, 2.0},
		});
		new JuliaSolver().solve(matrix, 1, 1.0);
	}

	@Test(expected = SingularMatrixException.class)
	public void testBlasInvertSingular() {
		assertTrue(Julia.load());
		var matrix = DenseMatrix.of(new double[][]{
			{1.0, -2.0},
			{-1.0, 2.0},
		});
		new JuliaSolver().invert(matrix);
	}

	// TODO: singularity checks are currently not supported for dense factorizations
	@Ignore
	@Test(expected = SingularMatrixException.class)
	public void testBlasFactorizeSingular() {
		assertTrue(Julia.load());
		var matrix = DenseMatrix.of(new double[][]{
			{1.0, -2.0},
			{-1.0, 2.0},
		});
		new JuliaSolver().factorize(matrix);
	}
}
