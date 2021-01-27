package org.openlca.core.matrix.solvers;

import org.openlca.core.matrix.format.Matrix;

public class SeqAgg {

	private final Matrix a;
	private final Matrix b;
	private final int refIdx;
	private final double scale;

	public SeqAgg(Matrix a, Matrix b, int refIdx, double demand) {
		this.a = a.copy();
		this.b = b.copy();
		this.refIdx = refIdx;
		this.scale = demand / a.get(refIdx, refIdx);
	}

	public double[] solve() {
		for (int sourceIdx = 0; sourceIdx < a.columns(); sourceIdx++) {
			if (sourceIdx == refIdx)
				continue;
			if (refIdx < sourceIdx)
				add(sourceIdx, refIdx);
			for (int targetIdx = sourceIdx + 1; targetIdx < a
					.columns(); targetIdx++) {
				add(sourceIdx, targetIdx);
			}
		}
		double[] refCol = b.getColumn(refIdx);
		for (int i = 0; i < refCol.length; i++)
			refCol[i] *= scale;
		return refCol;
	}

	/** Adds the source column to the target column. */
	private void add(int sourceIdx, int targetIdx) {
		double ts = a.get(sourceIdx, targetIdx);
		double st = a.get(targetIdx, sourceIdx);
		if (ts == 0 && st == 0)
			return; // independent columns
		double ss = a.get(sourceIdx, sourceIdx);
		double tt = a.get(targetIdx, targetIdx);
		if (ss == 0 || tt == 0)
			return; // TODO: log warning: no diagonal entry
		if (st == 0 && ts != 0) {
			// linear adding
			double factor = -ts / ss;
			add(sourceIdx, targetIdx, factor);
		} else if (st != 0 && ts != 0) {
			// loop adding
			double q = (ts / ss) * (st / tt);
			// if (q >= 1 || q < 0)
			// throw new RuntimeException("Non-convergent loop");
			double loopFactor = 1 / (1 - q);
			scaleColumn(targetIdx, loopFactor);
			double factor = -ts / ss;
			add(sourceIdx, targetIdx, loopFactor * factor);
		}
	}

	private void add(int sourceIdx, final int targetIdx, final double factor) {
		for (int row = 0; row < a.rows(); row++) {
			double val = a.get(row, sourceIdx);
			double addVal = val * factor;
			double oldEntry = a.get(row, targetIdx);
			a.set(row, targetIdx, oldEntry + addVal);
		}
		for (int row = 0; row < b.rows(); row++) {
			double val = b.get(row, sourceIdx);
			double addVal = val * factor;
			double oldEntry = b.get(row, targetIdx);
			b.set(row, targetIdx, oldEntry + addVal);
		}
	}

	private void scaleColumn(final int columnIndex, final double factor) {
		for (int row = 0; row < a.rows(); row++) {
			double val = a.get(row, columnIndex);
			if (val == 0)
				continue;
			a.set(row, columnIndex, val * factor);
		}
		for (int row = 0; row < b.rows(); row++) {
			double val = b.get(row, columnIndex);
			if (val == 0)
				continue;
			b.set(row, columnIndex, val * factor);
		}
	}

}
