package org.openlca.core.matrix.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public record CopyToTest(MatrixReader source, Matrix target) {

	@Parameterized.Parameters
	public static Collection<Object[]> setup() {
		var data = new double[][]{
			{1, 1, 1, 1},
			{1, 1, 1, 1},
			{1, 1, 1, 1}
		};

		var sources = List.of(
			DenseMatrix.of(data),
			HashPointMatrix.of(data),
			CSCMatrix.of(data),
			JavaMatrix.of(data));
		int n = 10;
		var targets = List.of(
			new DenseMatrix(n, n),
			new HashPointMatrix(n, n),
			new JavaMatrix(n, n));
		var pairs = new ArrayList<Object[]>();
		for (var source : sources) {
			for (var target : targets) {
				pairs.add(new Object[]{source, target});
			}
		}
		return pairs;
	}

	@Test
	public void testCopyOffset() {
		for (var rowOffset = 0; rowOffset <= 10; rowOffset++) {
			for (var colOffset = 0; colOffset <= 10; colOffset++) {
				var target = this.target.copy();
				source.copyTo(target, rowOffset, colOffset);
				for (int i = 0; i < 10; i++) {
					for (int j = 0; j < 10; j++) {
						if (i >= rowOffset
							&& i < (rowOffset + source.rows())
							&& j >= colOffset
							&& j < (colOffset + source.columns())) {
							assertEquals(1.0, target.get(i, j), 1e-16);
						} else {
							assertEquals(0.0, target.get(i, j), 1e-16);
						}
					}
				}
			}
		}
	}
}
