package org.openlca.core.results.solutions;

import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.solvers.Factorization;
import org.openlca.core.matrix.solvers.IMatrixSolver;

import gnu.trove.map.hash.TIntObjectHashMap;

public class LazySolutionProvider implements SolutionProvider {

	private final MatrixData data;
	private final IMatrixSolver solver;
	private final Factorization factorization;

	private final double[] scalingVector;
	private final double[] totalFlows;
	private final double[] totalImpacts;
	private final double totalCosts;

	private final TIntObjectHashMap<double[]> solutions;
	private final TIntObjectHashMap<double[]> intensities;
	private final TIntObjectHashMap<double[]> impacts;

	private LazySolutionProvider(MatrixData data, IMatrixSolver solver) {
		this.data = data;
		this.solver = solver;
		this.factorization = solver.factorize(data.techMatrix);

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
		var s = solutionOfOne(refIdx);
		var d = data.techIndex.getDemand();
		scalingVector = new double[s.length];
		for (int i = 0; i < s.length; i++) {
			scalingVector[i] = s[i] * d;
		}

		// calculate the total results
		totalFlows = data.enviMatrix != null
				? solver.multiply(data.enviMatrix, scalingVector)
				: null;
		totalImpacts = totalFlows != null && data.impactMatrix != null
				? solver.multiply(data.impactMatrix, totalFlows)
				: null;
		var tCosts = 0.0;
		if (data.costVector != null) {
			for (int i = 0; i < scalingVector.length; i++) {
				tCosts += data.costVector[i] * scalingVector[i];
			}
		}
		totalCosts = tCosts;
	}

	public static LazySolutionProvider create(
			MatrixData data,
			IMatrixSolver solver) {
		return new LazySolutionProvider(data, solver);
	}

	@Override
	public TechIndex techIndex() {
		return data.techIndex;
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
	public double[] solutionOfOne(int product) {
		var s = solutions.get(product);
		if (s != null)
			return s;
		s = factorization.solve(product, 1.0);
		solutions.put(product, s);
		return s;
	}

	@Override
	public boolean hasFlows() {
		return intensities != null;
	}

	@Override
	public double[] totalFlows() {
		return totalFlows == null
				? new double[0]
				: totalFlows;
	}

	@Override
	public double[] totalFlowsOfOne(int product) {
		if (intensities == null)
			return new double[0];
		var m = intensities.get(product);
		if (m != null)
			return m;
		var s = solutionOfOne(product);
		m = solver.multiply(data.enviMatrix, s);
		intensities.put(product, m);
		return m;
	}

	@Override
	public double totalFlowOfOne(int flow, int product) {
		if (intensities == null)
			return 0;
		return totalFlowsOfOne(product)[flow];
	}

	@Override
	public boolean hasImpacts() {
		return impacts != null;
	}

	@Override
	public double[] totalImpacts() {
		return totalImpacts == null
				? new double[0]
				: totalImpacts;
	}

	@Override
	public double[] totalImpactsOfOne(int product) {
		if (impacts == null)
			return new double[0];
		var h = impacts.get(product);
		if (h != null)
			return h;
		var g = totalFlowsOfOne(product);
		h = solver.multiply(data.impactMatrix, g);
		impacts.put(product, h);
		return h;
	}

	@Override
	public double totalImpactOfOne(int indicator, int product) {
		if (impacts == null)
			return 0;
		return totalImpactsOfOne(product)[indicator];
	}

	@Override
	public boolean hasCosts() {
		return data.costVector != null;
	}

	@Override
	public double totalCosts() {
		return totalCosts;
	}

	@Override
	public double totalCostsOfOne(int i) {
		if (data.costVector == null)
			return 0;
		var s = solutionOfOne(i);
		double c = 0.0;
		for (int j = 0; j < s.length; j++) {
			c += s[j] * data.costVector[j];
		}
		return c;
	}

	@Override
	public double loopFactorOf(int i) {
		var aii = data.techMatrix.get(i, i);
		var eii = solutionOfOne(i)[i];
		var f = aii * eii;
		return f == 0
				? 0
				: 1 / f;
	}

}
