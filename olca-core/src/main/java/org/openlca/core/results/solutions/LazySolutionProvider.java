package org.openlca.core.results.solutions;

import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.solvers.IMatrixSolver;

import gnu.trove.map.hash.TIntObjectHashMap;

public class LazySolutionProvider implements SolutionProvider {

	private final MatrixData data;
	private final IMatrixSolver solver;

	private final TIntObjectHashMap<double[]> solutions;
	private final TIntObjectHashMap<double[]> intensities;
	private final TIntObjectHashMap<double[]> impacts;

	private LazySolutionProvider(MatrixData data, IMatrixSolver solver) {
		this.data = data;
		this.solver = solver;
		solutions = new TIntObjectHashMap<double[]>();
		intensities = data.enviMatrix == null
				? null
				: new TIntObjectHashMap<double[]>();
		impacts = data.impactMatrix == null
				? null
				: new TIntObjectHashMap<double[]>();
	}

	public static LazySolutionProvider create(
			MatrixData data,
			IMatrixSolver solver) {
		return new LazySolutionProvider(data, solver);
	}

	@Override
	public double[] solution(int i) {
		var s = solutions.get(i);
		if (s != null)
			return s;
		s = solver.solve(data.techMatrix, i, 1.0);
		solutions.put(i, s);
		return s;
	}

	@Override
	public boolean hasIntensities() {
		return intensities != null;
	}

	@Override
	public double[] intensities(int i) {
		if (intensities == null)
			return new double[0];
		var m = intensities.get(i);
		if (m != null)
			return m;
		var s = solution(i);
		m = solver.multiply(data.enviMatrix, s);
		intensities.put(i, m);
		return m;
	}

	@Override
	public boolean hasImpacts() {
		return impacts != null;
	}

	@Override
	public double[] impacts(int i) {
		if (impacts == null)
			return new double[0];
		var h = impacts.get(i);
		if (h != null)
			return h;
		var g = intensities(i);
		h = solver.multiply(data.impactMatrix, g);
		impacts.put(i, h);
		return h;
	}

	@Override
	public boolean hasCosts() {
		return data.costVector != null;
	}

	@Override
	public double costs(int i) {
		if (data.costVector == null)
			return 0;
		var s = solution(i);
		double c = 0.0;
		for (int j = 0; j < s.length; j++) {
			c += s[j] * data.costVector[j];
		}
		return c;
	}

	@Override
	public double getLoopFactor(int i) {
		var aii = data.techMatrix.get(i, i);
		var eii = solution(i)[i];
		var f = aii * eii;
		return f == 0
				? 0
				: 1 / f;
	}

}
