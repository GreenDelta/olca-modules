package org.openlca.core.matrix;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.core.matrix.format.CscMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;

public class SparseMulTest {

	private static final double EPSILON = 1e-10;

	@Test
	public void testHashPointMultiplication() {
		// A = [[1, 2], [3, 4]]
		var a = HashPointMatrix.of(new double[][]{
				{1, 2},
				{3, 4}
		});

		// B = [[5, 6], [7, 8]]
		var b = HashPointMatrix.of(new double[][]{
				{5, 6},
				{7, 8}
		});

		// C = A * B = [[19, 22], [43, 50]]
		var c = a.multiply(b);

		assertEquals(19, c.get(0, 0), EPSILON);
		assertEquals(22, c.get(0, 1), EPSILON);
		assertEquals(43, c.get(1, 0), EPSILON);
		assertEquals(50, c.get(1, 1), EPSILON);
	}

	@Test
	public void testCscMultiplication() {
		// A = [[1, 2], [3, 4]]
		var a = CscMatrix.of(new double[][]{
				{1, 2},
				{3, 4}
		});

		// B = [[5, 6], [7, 8]]
		var b = CscMatrix.of(new double[][]{
				{5, 6},
				{7, 8}
		});

		// C = A * B = [[19, 22], [43, 50]]
		var c = a.multiply(b);

		assertEquals(19, c.get(0, 0), EPSILON);
		assertEquals(22, c.get(0, 1), EPSILON);
		assertEquals(43, c.get(1, 0), EPSILON);
		assertEquals(50, c.get(1, 1), EPSILON);
	}

	@Test
	public void testMixedCscHashPoint() {
		// A as CSC
		var a = CscMatrix.of(new double[][]{
				{1, 2},
				{3, 4}
		});

		// B as HashPoint
		var b = HashPointMatrix.of(new double[][]{
				{5, 6},
				{7, 8}
		});

		// C = A * B = [[19, 22], [43, 50]]
		var c = a.multiply(b);

		assertEquals(19, c.get(0, 0), EPSILON);
		assertEquals(22, c.get(0, 1), EPSILON);
		assertEquals(43, c.get(1, 0), EPSILON);
		assertEquals(50, c.get(1, 1), EPSILON);
	}

	@Test
	public void testMixedHashPointCsc() {
		// A as HashPoint
		var a = HashPointMatrix.of(new double[][]{
				{1, 2},
				{3, 4}
		});

		// B as CSC
		var b = CscMatrix.of(new double[][]{
				{5, 6},
				{7, 8}
		});

		// C = A * B = [[19, 22], [43, 50]]
		var c = a.multiply(b);

		assertEquals(19, c.get(0, 0), EPSILON);
		assertEquals(22, c.get(0, 1), EPSILON);
		assertEquals(43, c.get(1, 0), EPSILON);
		assertEquals(50, c.get(1, 1), EPSILON);
	}

	@Test
	public void testSparseWithZeros() {
		// Sparse matrix A = [[1, 0], [0, 4]]
		var a = HashPointMatrix.of(new double[][]{
				{1, 0},
				{0, 4}
		});

		// Sparse matrix B = [[0, 2], [3, 0]]
		var b = HashPointMatrix.of(new double[][]{
				{0, 2},
				{3, 0}
		});

		// C = A * B = [[0, 2], [12, 0]]
		var c = a.multiply(b);

		assertEquals(0, c.get(0, 0), EPSILON);
		assertEquals(2, c.get(0, 1), EPSILON);
		assertEquals(12, c.get(1, 0), EPSILON);
		assertEquals(0, c.get(1, 1), EPSILON);
	}

	@Test
	public void testNonSquareMatrices() {
		// A = [[1, 2, 3], [4, 5, 6]] (2x3)
		var a = HashPointMatrix.of(new double[][]{
				{1, 2, 3},
				{4, 5, 6}
		});

		// B = [[7], [8], [9]] (3x1)
		var b = HashPointMatrix.of(new double[][]{
				{7},
				{8},
				{9}
		});

		// C = A * B = [[50], [122]] (2x1)
		var c = a.multiply(b);

		assertEquals(2, c.rows());
		assertEquals(1, c.columns());
		assertEquals(50, c.get(0, 0), EPSILON);
		assertEquals(122, c.get(1, 0), EPSILON);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDimensionMismatch() {
		var a = new HashPointMatrix(2, 3);
		var b = new HashPointMatrix(2, 2); // wrong dimensions

		a.multiply(b);
	}

	@Test
	public void testIdentityMatrix() {
		// Identity matrix
		var identity = HashPointMatrix.of(new double[][]{
				{1, 0, 0},
				{0, 1, 0},
				{0, 0, 1}
		});

		// Another matrix
		var a = HashPointMatrix.of(new double[][]{
				{1, 2, 3},
				{4, 5, 6},
				{7, 8, 9}
		});

		// A * I = A
		var c = a.multiply(identity);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				assertEquals(a.get(i, j), c.get(i, j), EPSILON);
			}
		}

		// I * A = A
		var d = identity.multiply(a);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				assertEquals(a.get(i, j), d.get(i, j), EPSILON);
			}
		}
	}
}
