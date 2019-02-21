package org.openlca.core.matrix;

import org.openlca.core.math.NumberGenerator;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.model.UncertaintyType;

import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * An UMatrix is a matrix with uncertainty distributions.
 */
public class UMatrix {

	private final TIntObjectHashMap<TIntObjectHashMap<UCell>> data;

	public UMatrix() {
		data = new TIntObjectHashMap<>(
				Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR,
				-1);
	}

	/**
	 * Set the given cell at the given position. null-values are ignored, so you
	 * cannot delete an existing value with this method.
	 */
	public void set(int row, int col, UCell val) {
		if (val == null)
			return;
		TIntObjectHashMap<UCell> rowm = data.get(row);
		if (rowm == null) {
			rowm = new TIntObjectHashMap<>(
					Constants.DEFAULT_CAPACITY,
					Constants.DEFAULT_LOAD_FACTOR,
					-1);
			data.put(row, rowm);
		}
		rowm.put(col, val);
	}

	/**
	 * Generates new values and sets them to the given matrix.
	 */
	public void generate(IMatrix m) {
		TIntObjectIterator<TIntObjectHashMap<UCell>> rows = data.iterator();
		while (rows.hasNext()) {
			rows.advance();
			int row = rows.key();
			TIntObjectIterator<UCell> cols = rows.value().iterator();
			while (cols.hasNext()) {
				cols.advance();
				int col = cols.key();
				UCell cell = cols.value();
				m.set(row, col, cell.next());
			}
		}
	}

	/**
	 * UCell is a single cell in an uncertainty matrix.
	 */
	public static class UCell {

		/** The first parameter of the uncertainty distribution. */
		public double param1;

		/** The second parameter of the uncertainty distribution. */
		public double param2;

		/**
		 * The third parameter of the uncertainty distribution; may not be used.
		 */
		public double param3;

		/**
		 * An optional conversion factor that is applied after a new value is
		 * generated.
		 */
		public double factor = 1.0;

		/** When true, the sign of the generated value is changed. */
		public boolean swapSign;

		/** The uncertainty distribution type of this cell. */
		public UncertaintyType type;

		/*
		 * TODO: we have to think about stateless functions here; also with a
		 * shared instance of Random; see also this issue:
		 * https://github.com/GreenDelta/olca-app/issues/62
		 */
		private NumberGenerator gen;

		/** Generate the next number of */
		public double next() {
			if (gen == null) {
				gen = generator();
			}
			double val = gen.next() * factor;
			if (val == 0)
				return 0;
			return swapSign ? -val : val;
		}

		private NumberGenerator generator() {
			if (type == null) {
				return NumberGenerator.discrete(param1);
			}
			switch (type) {
			case LOG_NORMAL:
				return NumberGenerator.logNormal(
						param1, param2);
			case NORMAL:
				return NumberGenerator.normal(
						param1, param2);
			case TRIANGLE:
				return NumberGenerator.triangular(
						param1, param2, param3);
			case UNIFORM:
				return NumberGenerator.uniform(
						param1, param2);
			default:
				return NumberGenerator.discrete(param1);
			}
		}
	}
}
