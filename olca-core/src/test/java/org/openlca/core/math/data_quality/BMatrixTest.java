package org.openlca.core.math.data_quality;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class BMatrixTest {

	private BMatrix matrix;

	@Before
	public void setup() {
		// create a matrix [ 1 2 3 ; 4 5 6]
		matrix = new BMatrix(2, 3);
		for (int i = 1; i < 7; i++) {
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
		assertArrayEquals(new int[] {1, 4}, matrix.getColumn(0));
		assertArrayEquals(new int[] {2, 5}, matrix.getColumn(1));
		assertArrayEquals(new int[] {3, 6}, matrix.getColumn(2));
	}

	@Test
	public void testGetRow() {
		assertArrayEquals(new int[] {1, 2, 3}, matrix.getRow(0));
		assertArrayEquals(new int[] {4, 5, 6}, matrix.getRow(1));
	}
}
