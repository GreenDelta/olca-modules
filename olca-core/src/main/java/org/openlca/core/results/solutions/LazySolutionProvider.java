package org.openlca.core.results.solutions;

import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.solvers.IMatrixSolver;

import gnu.trove.map.hash.TIntObjectHashMap;

public class LazySolutionProvider implements SolutionProvider {

	private final MatrixData data;
	private final IMatrixSolver solver;

	private final double[] scalingVector;


	private final TIntObjectHashMap<double[]> solutions;
	private final TIntObjectHashMap<double[]> intensities;
	private final TIntObjectHashMap<double[]> impacts;

	private LazySolutionProvider(MatrixData data, IMatrixSolver solver) {
		this.data = data;
		this.solver = solver;
		solutions = new TIntObjectHashMap<>();
		intensities = data.enviMatrix == null
				? null
				: new TIntObjectHashMap<>();
		impacts = data.impactMatrix == null
				? null
				: new TIntObjectHashMap<>();

		// calculate the scaling vector
		var refIdx = data.techIndex.getIndex(
				data.techIndex.getRefFlow());
		var s = solution(refIdx);
		var d = data.techIndex.getDemand();
		scalingVector = new double[s.length];
		for (int i = 0; i < s.length; i++) {
			scalingVector[i] = s[i] * d;
		}
	}

	public static LazySolutionProvider create(
			MatrixData data,
			IMatrixSolver solver) {
		return new LazySolutionProvider(data, solver);
	}

	@Override
	public double[] scalingVector() {
		return scalingVector;
	}

	@Override
	public double[] columnOfA(int product) {
		return data.techMatrix.getColumn(product);
	}

	@Override
	public double valueOfA(int row, int col) {
		return data.techMatrix.get(row, col);
	}

	@Override
	public double scaledValueOfA(int row, int col) {
		var s = scalingVector[col];
		return s * data.techMatrix.get(row, col);
	}

	@Override
	public double[] solution(int product) {
		var s = solutions.get(product);
		if (s != null)
			return s;
		s = solver.solve(data.techMatrix, product, 1.0);
		solutions.put(product, s);
		return s;
	}

	@Override
	public boolean hasIntensities() {
		return intensities != null;
	}

	@Override
	public double[] intensities(int product) {
		if (intensities == null)
			return new double[0];
		var m = intensities.get(product);
		if (m != null)
			return m;
		var s = solution(product);
		m = solver.multiply(data.enviMatrix, s);
		intensities.put(product, m);
		return m;
	}

	@Override
	public double intensity(int flow, int product) {
		if (intensities == null)
			return 0;
		return intensities(product)[flow];
	}

	@Override
	public boolean hasImpacts() {
		return impacts != null;
	}

	@Override
	public double[] impacts(int product) {
		if (impacts == null)
			return new double[0];
		var h = impacts.get(product);
		if (h != null)
			return h;
		var g = intensities(product);
		h = solver.multiply(data.impactMatrix, g);
		impacts.put(product, h);
		return h;
	}

	@Override
	public double impact(int indicator, int product) {
		if (impacts == null)
			return 0;
		return impacts(product)[indicator];
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
