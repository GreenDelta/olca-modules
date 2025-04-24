package org.openlca.core.library.export;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.Process;

/// In a library, all processes are mono-functional with a reference flow of
/// exactly 1 unit of product output or waste input. If a process in the
/// database has a reference amount of `a != 1`, it is scaled by `1/a`.
public class Scaler {

	private final Map<String, Double> factors;

	private Scaler(Map<String, Double> factors) {
		this.factors = factors;
	}

	// Scales the matrix data if necessary and creates a scaler that can be
	// then used in the JSON export.
	public static Scaler scale(MatrixData data) {
		if (data == null || data.techIndex == null || data.techMatrix == null)
			return new Scaler(Map.of());

		var techIdx = data.techIndex;
		var techMatrix = data.techMatrix.asMutable();
		var enviMatrix = data.enviMatrix != null
				? data.enviMatrix.asMutable()
				: null;
		int n = techMatrix.columns();
		var scaler = new Scaler(new HashMap<>());

		for (int j = 0; j < n; j++) {
			double v = Math.abs(techMatrix.get(j, j));
			if (v == 0 || v == 1)
				continue;
			double f = 1 / v;
			var techFlow = techIdx.at(j);
			scaler.put(techFlow, f);

			scaleColumn(techMatrix, j, f, true);
			if (enviMatrix != null) {
				scaleColumn(enviMatrix, j, f, false);
			}

			if (data.costVector != null) {
				double c = data.costVector[j];
				if (c != 0) {
					data.costVector[j] = f * c;
				}
			}
		}

		if (scaler.factors.isEmpty())
			return scaler;

		data.techMatrix = techMatrix;
		data.enviMatrix = enviMatrix;
		return scaler;
	}

	private static void scaleColumn(
			Matrix m, int j, double f, boolean isTech
	) {
		for (int i = 0; i < m.rows(); i++) {
			double v = m.get(i, j);
			if (v == 0)
				continue;
			if (i == j && isTech) {
				m.set(i, j, 1.0);
				continue;
			}
			m.set(i, j, f * v);
		}
	}

	private void put(TechFlow techFlow, double f) {
		if (techFlow == null)
			return;
		var p = techFlow.provider();
		if (p == null || p.refId == null)
			return;
		factors.put(p.refId, f);
	}

	/// Scales the relevant process meta-data before writing them to a JSON
	/// packages. Currently, it is only the activity values of the social aspects
	/// that need to be scaled.
	void scale(Process p) {
		if (p == null || p.socialAspects.isEmpty())
			return;
		Double f = factors.get(p.refId);
		if (f == null)
			return;
		double fi = f;
		if (fi == 1.0)
			return;
		for (var a : p.socialAspects) {
			a.activityValue *= fi;
		}
	}
}
