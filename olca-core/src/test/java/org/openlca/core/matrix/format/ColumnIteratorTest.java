package org.openlca.core.matrix.format;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class ColumnIteratorTest {

	@Test
	public void testNonZeros() {

		var data = new double[][]{
				{1, 0, 2, 0, 3},
				{0, 4, 0, 5, 0},
				{6, 0, 7, 0, 8},
				{0, 9, 0, 0, 0},
		};

		var dense = DenseMatrix.of(data);
		var points = HashPointMatrix.of(data);
		var csc = points.compress();
		var java = JavaMatrix.of(data);
		var matrices = List.of(
				dense, points, csc, java);

		for (var matrix : matrices) {
			for (var col = 0; col < 5; col++) {
				var it = ColumnIterator.of(matrix, col);
				var expected = Expected.of(it);
				it.eachNonZero(expected::assertEquals);
				expected.assertAllChecked();
			}
		}
	}

	private static class Expected {

		private final ColumnIterator it;
		private final Map<Integer, Double> values = new HashMap<>();
		private final Set<Integer> checked = new HashSet<>();

		static Expected of(ColumnIterator it) {
			return switch (it.column()) {
				case 0 -> new Expected(it).entry(0, 1).entry(2, 6);
				case 1 -> new Expected(it).entry(1, 4).entry(3, 9);
				case 2 -> new Expected(it).entry(0, 2).entry(2, 7);
				case 3 -> new Expected(it).entry(1, 5);
				case 4 -> new Expected(it).entry(0, 3).entry(2, 8);
				default -> throw new IllegalArgumentException(
						"undefined column: " + it.column());
			};
		}

		private Expected(ColumnIterator it) {
			this.it = it;
		}

		Expected entry(int row, double value) {
			values.put(row, value);
			return this;
		}

		void assertEquals(int row, double value) {
			var expected = values.get(row);
			if (expected == null) {
				Assert.fail(it.getClass().getSimpleName() +
						": expected no value at [" + row + ", " +
						it.column() + "] but was " + value);
				return;
			}
			Assert.assertEquals(expected, value, 1e-16);
			checked.add(row);
		}

		void assertAllChecked() {
			Assert.assertEquals(checked.size(), values.size());
		}
	}
}
