package org.openlca.eigen.solvers;

import org.openlca.core.math.IMatrix;
import org.openlca.eigen.Numbers;

public class SeqAgg {

	private final IMatrix a;
	private final IMatrix b;
	private final int refIdx;
	private final double scale;

	public SeqAgg(IMatrix a, IMatrix b, int refIdx, double demand) {
		this.a = a.copy();
		this.b = b.copy();
		this.refIdx = refIdx;
		this.scale = demand / a.getEntry(refIdx, refIdx);
	}

	public double[] solve() {
		for (int sourceIdx = 0; sourceIdx < a.getColumnDimension(); sourceIdx++) {
			if (sourceIdx == refIdx)
				continue;
			if (refIdx < sourceIdx)
				add(sourceIdx, refIdx);
			for (int targetIdx = sourceIdx + 1; targetIdx < a
					.getColumnDimension(); targetIdx++) {
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
		double ts = a.getEntry(sourceIdx, targetIdx);
		double st = a.getEntry(targetIdx, sourceIdx);
		if (Numbers.isZero(ts) && Numbers.isZero(st))
			return; // independent columns
		double ss = a.getEntry(sourceIdx, sourceIdx);
		double tt = a.getEntry(targetIdx, targetIdx);
		if (Numbers.isZero(ss) || Numbers.isZero(tt))
			return; // TODO: log warning: no diagonal entry
		if (Numbers.isZero(st) && !Numbers.isZero(ts)) {
			// linear adding
			double factor = -ts / ss;
			add(sourceIdx, targetIdx, factor);
		} else if (!Numbers.isZero(st) && !Numbers.isZero(ts)) {
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
		for (int row = 0; row < a.getRowDimension(); row++) {
			double val = a.getEntry(row, sourceIdx);
			double addVal = val * factor;
			double oldEntry = a.getEntry(row, targetIdx);
			a.setEntry(row, targetIdx, oldEntry + addVal);
		}
		for (int row = 0; row < b.getRowDimension(); row++) {
			double val = b.getEntry(row, sourceIdx);
			double addVal = val * factor;
			double oldEntry = b.getEntry(row, targetIdx);
			b.setEntry(row, targetIdx, oldEntry + addVal);
		}
	}

	private void scaleColumn(final int columnIndex, final double factor) {
		for (int row = 0; row < a.getRowDimension(); row++) {
			double val = a.getEntry(row, columnIndex);
			if (Numbers.isZero(val))
				continue;
			a.setEntry(row, columnIndex, val * factor);
		}
		for (int row = 0; row < b.getRowDimension(); row++) {
			double val = b.getEntry(row, columnIndex);
			if (Numbers.isZero(val))
				continue;
			b.setEntry(row, columnIndex, val * factor);
		}
	}

}
