package org.openlca.core.matrix.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class MatrixBuilderTest {

	@Test
	public void testSparseDiagonal() {
		int size = 1_000_000;
		MatrixBuilder b = new MatrixBuilder();
		for (int i = 0; i < size; i++) {
			b.set(i, i, i);
		}
		Matrix m = b.finish();
		assertEquals(size, m.rows());
		assertEquals(size, m.columns());
		for (int i = 0; i < size; i++) {
			double val = m.get(i, i);
			assertEquals(i, val, 1e-16);
		}
		assertEquals(HashPointMatrix.class, m.getClass());
	}

	@Test
	public void testFullDense() {
		MatrixBuilder b = new MatrixBuilder(0.1, 500_000);
		for (int row = 0; row < 2500; row++) {
			for (int col = 0; col < 1500; col++) {
				b.set(row, col, row * col);
			}
		}
		Matrix m = b.finish();
		assertEquals(2500, m.rows());
		assertEquals(1500, m.columns());
		assertEquals(DenseMatrix.class, m.getClass());
		for (int row = 0; row < 2500; row++) {
			for (int col = 0; col < 1500; col++) {
				double val = m.get(row, col);
				assertEquals((double) row * col, val, 1e-16);
			}
		}
	}

	@Test
	public void testFullDenseMinSize() {
		MatrixBuilder b = new MatrixBuilder(0.1, 500_000);
		b.ensureSize(2000, 1000);
		for (int row = 0; row < 2500; row++) {
			for (int col = 0; col < 1500; col++) {
				b.set(row, col, row * col);
			}
		}
		Matrix m = b.finish();
		assertEquals(2500, m.rows());
		assertEquals(1500, m.columns());
		assertEquals(DenseMatrix.class, m.getClass());
		for (int row = 0; row < 2500; row++) {
			for (int col = 0; col < 1500; col++) {
				double val = m.get(row, col);
				assertEquals((double) row * col, val, 1e-16);
			}
		}
	}

	@Test
	public void testAddAndNnzTracking() {
		var b = new MatrixBuilder(0.5, 10);
		// Add to empty
		b.add(0, 0, 1.0);
		// Accumulate
		b.add(0, 0, 1.0);

		var m = b.finish();
		assertEquals(2.0, m.get(0, 0), 1e-16);

		// Test decrementing NNZ by adding negative
		b = new MatrixBuilder(0.5, 2);
		b.set(0, 0, 1.0);
		b.add(0, 0, -1.0); // Should be 0 now
		b.set(1, 1, 1.0);

		m = b.finish();
		assertEquals(0.0, m.get(0, 0), 1e-16);
		// If NNZ tracking failed, it might have triggered dense switch prematurely
		assertEquals(HashPointMatrix.class, m.getClass());
	}

	@Test
	public void testIsEmptyAndNegativeIndices() {
		var b = new MatrixBuilder();
		assertTrue(b.isEmpty());

		b.set(-1, -1, 1.0);
		assertTrue(b.isEmpty());

		b.add(0, 0, 1.0);
		assertFalse(b.isEmpty());
	}
}
