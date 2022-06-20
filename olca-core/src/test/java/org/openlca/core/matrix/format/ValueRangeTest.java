package org.openlca.core.matrix.format;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RunWith(Parameterized.class)
public record ValueRangeTest(Matrix matrix) {

	private static final int ROWS = 7;
	private static final int COLS = 10;

	@Parameterized.Parameters
	public static Collection<Matrix> setup() {
		return List.of(
			new DenseMatrix(ROWS, COLS),
			new HashPointMatrix(ROWS, COLS),
			new JavaMatrix(ROWS, COLS)
		);
	}

	@Test
	public void testSetRow() {
		assertAllZero();
		var rand = ThreadLocalRandom.current();
		for (int row = 0; row < ROWS; row++) {
			var values = rand.doubles(COLS).toArray();
			matrix.setRow(row, values);
			assertArrayEquals(values, matrix.getRow(row), 1e-16);
		}
		for (int row = 0; row < ROWS; row++) {
			matrix.setRow(row, new double[COLS]);
		}
		assertAllZero();
	}

	@Test
	public void testRowRange() {
		assertAllZero();
		var rand = ThreadLocalRandom.current();
		for (int row = 0; row < ROWS; row++) {
			int offset = rand.nextInt(1, 4);
			var values = rand.doubles(COLS - (2L * offset)).toArray();
			matrix.setRowRange(row, offset, values);
			var rowVals = matrix.getRow(row);
			for (int i = 0; i < rowVals.length; i++) {
				if (i >= offset && i < COLS - offset) {
					assertEquals(values[i - offset], rowVals[i], 1e-16);
				} else {
					assertEquals(0, rowVals[i], 1e-16);
				}
			}
			matrix.setRowRange(row, offset, new double[values.length]);
		}
		assertAllZero();
	}

	@Test
	public void testColumnRange() {
		assertAllZero();
		var rand = ThreadLocalRandom.current();
		for (int col = 0; col < COLS; col++) {
			int offset = rand.nextInt(1, 3);
			var values = rand.doubles(ROWS - (2L * offset)).toArray();
			matrix.setColumnRange(col, offset, values);
			var colVals = matrix.getColumn(col);
			for (int i = 0; i < colVals.length; i++) {
				if (i >= offset && i < ROWS - offset) {
					assertEquals(values[i - offset], colVals[i], 1e-16);
				} else {
					assertEquals(0, colVals[i], 1e-16);
				}
			}
			matrix.setColumnRange(col, offset, new double[values.length]);
		}
		assertAllZero();
	}

	@Test
	public void testSetColumn() {
		assertAllZero();
		var rand = ThreadLocalRandom.current();
		for (int col = 0; col < COLS; col++) {
			var values = rand.doubles(ROWS).toArray();
			matrix.setColumn(col, values);
			assertArrayEquals(values, matrix.getColumn(col), 1e-16);
		}
		for (int col = 0; col < COLS; col++) {
			matrix.setColumn(col, new double[ROWS]);
		}
		assertAllZero();
	}

	private void assertAllZero() {
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				assertEquals(0, matrix.get(row, col), 1e-16);
			}
		}
	}
}
