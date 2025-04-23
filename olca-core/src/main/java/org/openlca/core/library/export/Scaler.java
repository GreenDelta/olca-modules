package org.openlca.core.library.export;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.matrix.MatrixData;
import org.openlca.core.model.Process;

import gnu.trove.map.hash.TLongIntHashMap;

/// In a library, all processes are mono-functional with a reference flow of
/// exactly 1 unit of product output or waste input. If a process in the
/// database has a reference amount of `a != 1`, it is scaled by `1/a`.
class Scaler {

	private final Map<String, Double> factors;

	private Scaler(Map<String, Double> factors) {
		this.factors = factors;
	}

	// Scales the matrix data if necessary and creates a scaler that can be
	// then used in the JSON export.
	static Scaler scale(MatrixData data) {
		if (data == null || data.techIndex == null || data.techMatrix == null)
			return new Scaler(Map.of());

		var techMatrix = data.techMatrix.asMutable();
		int n = techMatrix.columns();
		double[] factors = null;
		HashMap<String, Double> factorMap = null;

		for (int i = 0; i < n; i++) {
			double v = techMatrix.get(i, i);
			if (v == 0 || v == 1)
				continue;
			if (factors == null) {
				factors = new double[n];
				Arrays.fill(factors, 1.0);
				factorMap = new HashMap<>();
			}
			double f = 1 / v;
			factors[i] = f;


		}

		return new Scaler(factorMap);
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
		double fi = f.doubleValue();
		if (fi == 1.0)
			return;
		for (var a : p.socialAspects) {
			a.activityValue *= fi;
		}
	}
}
