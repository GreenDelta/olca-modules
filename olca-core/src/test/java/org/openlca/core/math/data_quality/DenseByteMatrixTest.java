package org.openlca.core.math.data_quality;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.openlca.core.matrix.format.DenseByteMatrix;

public class DenseByteMatrixTest {

	private DenseByteMatrix matrix;

	@Before
	public void setup() {
		// create a matrix [ 1 2 3 ; 4 5 6]
		matrix = new DenseByteMatrix(2, 3);
		for (byte i = 1; i < 7; i++) {
			int row = i < 4 ? 0 : 1;
			int col = (i - 1) % 3;
			matrix.set(row, col, i);
		}
	}

	@Test
	public void testGet() {
		for (int row = 0; row < 2; row++) {
			for (int col = 0; col < 3; col++) {
				int expected = col + 1 + row * 3;
				assertEquals(expected, matrix.get(row, col));
			}
		}
	}

	@Test
	public void testGetColumn() {
		assertArrayEquals(new byte[] {1, 4}, matrix.getColumn(0));
		assertArrayEquals(new byte[] {2, 5}, matrix.getColumn(1));
		assertArrayEquals(new byte[] {3, 6}, matrix.getColumn(2));
	}

	@Test
	public void testGetRow() {
		assertArrayEquals(new byte[] {1, 2, 3}, matrix.getRow(0));
		assertArrayEquals(new byte[] {4, 5, 6}, matrix.getRow(1));
	}
}
