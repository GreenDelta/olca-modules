package org.openlca.core.matrix.format;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public record ReadRowColumnTest(MatrixReader matrix) {

	@Parameterized.Parameters
	public static Collection<MatrixReader> setup() {
		var data = new double[][]{
			{0, 2, 4},
			{1, 3, 5}
		};
		return List.of(
			DenseMatrix.of(data),
			HashPointMatrix.of(data),
			CSCMatrix.of(data),
			JavaMatrix.of(data));
	}

	@Test
	public void testReadRow() {
		var buffer = new double[3];
		matrix.readRow(0, buffer);
		check(buffer, 0, 2, 4);
		matrix.readRow(1, buffer);
		check(buffer, 1, 3, 5);
	}

	@Test
	public void testReadColumn() {
		var buffer = new double[2];
		matrix.readColumn(0, buffer);
		check(buffer, 0, 1);
		matrix.readColumn(1, buffer);
		check(buffer, 2, 3);
		matrix.readColumn(2, buffer);
		check(buffer, 4, 5);
	}

	private void check(double[] buffer, double... values) {
		assertArrayEquals(buffer, values, 1e-16);
	}

}
