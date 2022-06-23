package org.openlca.core.matrix.format;

import static org.junit.Assert.*;

import org.junit.Test;

public class CopyToTest {

	@Test
	public void testCopyOffset() {

		var source = HashPointMatrix.of(new double[][] {
			{1,1,1,1},
			{1,1,1,1},
			{1,1,1,1}
		});

		for (var rowOffset = 0; rowOffset <= 10; rowOffset++) {
			for (var colOffset = 0; colOffset <= 10; colOffset++) {
				var target = new DenseMatrix(10, 10);
				source.copyTo(target, rowOffset, colOffset);
				// System.out.println(target);

				for (int i = 0; i < 10; i++) {
					for (int j = 0; j < 10; j++) {
						if (i >= rowOffset
							&& i < (rowOffset + source.rows)
							&& j >= colOffset
							&& j < (colOffset + source.cols)) {
							assertEquals(1.0, target.get(i, j), 1e-16);
						}else {
							assertEquals(0.0, target.get(i, j), 1e-16);
						}
					}
				}
			}
		}
	}
}
