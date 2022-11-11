package org.openlca.core.math.data_quality;

import org.openlca.core.matrix.format.ByteMatrix;
import org.openlca.core.matrix.format.DenseByteMatrix;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.HashPointByteMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.Matrix;

class Accu {

	private final AggregationType aggType;
	private final boolean ceiling;
	private final boolean zeroToMax;
	private final byte maxScore;

	private final Matrix[] accuValues;
	private final Matrix totalWeights;

	Accu(DQSetup setup, int rows) {
		this(setup, rows, 1);
	}

	Accu(DQSetup setup, int rows, int cols) {
		this.aggType = setup.aggregationType != null
				? setup.aggregationType
				: AggregationType.MAXIMUM;
		this.ceiling = setup.ceiling;
		this.zeroToMax = setup.naHandling == NAHandling.USE_MAX;
		maxScore = (byte) setup.exchangeSystem.getScoreCount();
		int dqiCount = setup.exchangeSystem.indicators.size();

		accuValues = new Matrix[dqiCount];
		for (int dqi = 0; dqi < dqiCount; dqi++) {
			accuValues[dqi] = new DenseMatrix(rows, cols);
		}
		totalWeights = aggType != AggregationType.MAXIMUM
				? new DenseMatrix(rows, cols)
				: null;
	}

	void add(int row, byte[] dqValues, double weight) {
		add(row, 0, dqValues, weight);
	}

	void add(int row, int col, byte[] dqValues, double weight) {
		if (dqValues == null || weight == 0)
			return;

		double w = aggType == AggregationType.WEIGHTED_SQUARED_AVERAGE
				? Math.pow(weight, 2)
				: Math.abs(weight);

		if (aggType != AggregationType.MAXIMUM) {
			double next = w + totalWeights.get(row, col);
			totalWeights.set(row, col, next);
		}

		for (int dqi = 0; dqi < dqValues.length; dqi++) {
			double dqVal = dqValues[dqi];
			if (dqVal == 0) {
				if (zeroToMax) {
					dqVal = maxScore;
				} else {
					continue;
				}
			}

			var matrix = accuValues[dqi];
			double previous = matrix.get(row, col);
			double next = aggType == AggregationType.MAXIMUM
					? Math.max(previous, dqVal)
					: previous + (dqVal * w);
			matrix.set(row, col, next);
		}
	}

	ByteMatrix[] finish() {
		var byteMatrices = new ByteMatrix[accuValues.length];
		for (int i = 0; i < accuValues.length; i++) {
			var m = accuValues[i];
			var bytes = m.isSparse()
					? new HashPointByteMatrix(m.rows(), m.columns())
					: new DenseByteMatrix(m.rows(), m.columns());
			m.iterate((row, col, value) -> {
				if (value == 0)
					return;
				byte b = aggType == AggregationType.MAXIMUM
						? min(maxScore, (byte) value)
						: norm(row, col, value);
				bytes.set(row, col, b);
			});
			byteMatrices[i] = bytes;
		}
		return byteMatrices;
	}

	private byte norm(int row, int col, double value) {
		double w = totalWeights.get(row, col);
		if (w == 0)
			return maxScore;
		byte b = ceiling
				? (byte) Math.round(Math.ceil(value / w))
				: (byte) Math.round(value / w);
		return min(b, maxScore);
	}

	private static byte min(byte b1, byte b2) {
		return b1 > b2 ? b2 : b1;
	}
}

