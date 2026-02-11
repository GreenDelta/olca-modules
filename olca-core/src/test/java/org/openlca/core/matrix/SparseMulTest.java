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
		var a = new HashPointMatrix(2, 2);
		a.set(0, 0, 1);
		a.set(0, 1, 2);
		a.set(1, 0, 3);
		a.set(1, 1, 4);

		// B = [[5, 6], [7, 8]]
		var b = new HashPointMatrix(2, 2);
		b.set(0, 0, 5);
		b.set(0, 1, 6);
		b.set(1, 0, 7);
		b.set(1, 1, 8);

		// C = A * B = [[19, 22], [43, 50]]
		var c = SparseMul.multiply(a, b);

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
		var c = SparseMul.multiply(a, b);

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
		var b = new HashPointMatrix(2, 2);
		b.set(0, 0, 5);
		b.set(0, 1, 6);
		b.set(1, 0, 7);
		b.set(1, 1, 8);

		// C = A * B = [[19, 22], [43, 50]]
		var c = SparseMul.multiply(a, b);

		assertEquals(19, c.get(0, 0), EPSILON);
		assertEquals(22, c.get(0, 1), EPSILON);
		assertEquals(43, c.get(1, 0), EPSILON);
		assertEquals(50, c.get(1, 1), EPSILON);
	}

	@Test
	public void testMixedHashPointCsc() {
		// A as HashPoint
		var a = new HashPointMatrix(2, 2);
		a.set(0, 0, 1);
		a.set(0, 1, 2);
		a.set(1, 0, 3);
		a.set(1, 1, 4);

		// B as CSC
		var b = CscMatrix.of(new double[][]{
				{5, 6},
				{7, 8}
		});

		// C = A * B = [[19, 22], [43, 50]]
		var c = SparseMul.multiply(a, b);

		assertEquals(19, c.get(0, 0), EPSILON);
		assertEquals(22, c.get(0, 1), EPSILON);
		assertEquals(43, c.get(1, 0), EPSILON);
		assertEquals(50, c.get(1, 1), EPSILON);
	}

	@Test
	public void testSparseWithZeros() {
		// Sparse matrix A = [[1, 0], [0, 4]]
		var a = new HashPointMatrix(2, 2);
		a.set(0, 0, 1);
		a.set(1, 1, 4);

		// Sparse matrix B = [[0, 2], [3, 0]]
		var b = new HashPointMatrix(2, 2);
		b.set(0, 1, 2);
		b.set(1, 0, 3);

		// C = A * B = [[0, 2], [12, 0]]
		var c = SparseMul.multiply(a, b);

		assertEquals(0, c.get(0, 0), EPSILON);
		assertEquals(2, c.get(0, 1), EPSILON);
		assertEquals(12, c.get(1, 0), EPSILON);
		assertEquals(0, c.get(1, 1), EPSILON);
	}

	@Test
	public void testNonSquareMatrices() {
		// A = [[1, 2, 3], [4, 5, 6]] (2x3)
		var a = new HashPointMatrix(2, 3);
		a.set(0, 0, 1);
		a.set(0, 1, 2);
		a.set(0, 2, 3);
		a.set(1, 0, 4);
		a.set(1, 1, 5);
		a.set(1, 2, 6);

		// B = [[7], [8], [9]] (3x1)
		var b = new HashPointMatrix(3, 1);
		b.set(0, 0, 7);
		b.set(1, 0, 8);
		b.set(2, 0, 9);

		// C = A * B = [[50], [122]] (2x1)
		var c = SparseMul.multiply(a, b);

		assertEquals(2, c.rows());
		assertEquals(1, c.columns());
		assertEquals(50, c.get(0, 0), EPSILON);
		assertEquals(122, c.get(1, 0), EPSILON);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDimensionMismatch() {
		var a = new HashPointMatrix(2, 3);
		var b = new HashPointMatrix(2, 2); // wrong dimensions

		SparseMul.multiply(a, b);
	}

	@Test
	public void testIdentityMatrix() {
		// Identity matrix
		var identity = new HashPointMatrix(3, 3);
		identity.set(0, 0, 1);
		identity.set(1, 1, 1);
		identity.set(2, 2, 1);

		// Another matrix
		var a = new HashPointMatrix(3, 3);
		a.set(0, 0, 1);
		a.set(0, 1, 2);
		a.set(0, 2, 3);
		a.set(1, 0, 4);
		a.set(1, 1, 5);
		a.set(1, 2, 6);
		a.set(2, 0, 7);
		a.set(2, 1, 8);
		a.set(2, 2, 9);

		// A * I = A
		var c = SparseMul.multiply(a, identity);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				assertEquals(a.get(i, j), c.get(i, j), EPSILON);
			}
		}

		// I * A = A
		var d = SparseMul.multiply(identity, a);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				assertEquals(a.get(i, j), d.get(i, j), EPSILON);
			}
		}
	}
}
