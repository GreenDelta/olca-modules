package org.openlca.core.results.solutions;

import org.openlca.core.matrix.DIndex;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.Factorization;
import org.openlca.core.matrix.solvers.IMatrixSolver;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public class LazySolutionProvider implements SolutionProvider {

	private final MatrixData data;
	private final IMatrixSolver solver;
	private final Factorization factorization;

	private final double[] scalingVector;
	private double[] totalRequirements;
	private final TIntObjectHashMap<double[]> solutions;

	private IMatrix directFlows;
	private final double[] totalFlows;
	private final TIntObjectHashMap<double[]> totalFlowsOfOne;

	private IMatrix directImpacts;
	private final double[] totalImpacts;
	private final TIntObjectHashMap<double[]> totalImpactsOfOne;

	private final double[] directCosts;
	private final double totalCosts;

	private LazySolutionProvider(MatrixData data, IMatrixSolver solver) {
		this.data = data;
		this.solver = solver;
		this.factorization = solver.factorize(data.techMatrix);

		solutions = new TIntObjectHashMap<>();
		totalFlowsOfOne = data.flowMatrix == null
				? null
				: new TIntObjectHashMap<>();
		totalImpactsOfOne = data.impactMatrix == null
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
		totalFlows = data.flowMatrix != null
				? solver.multiply(data.flowMatrix, scalingVector)
				: null;
		totalImpacts = totalFlows != null && data.impactMatrix != null
				? solver.multiply(data.impactMatrix, totalFlows)
				: null;

		// costs
		if (data.costVector == null) {
			totalCosts = 0;
			directCosts = null;
		} else {
			var tCosts = 0.0;
			directCosts = new double[scalingVector.length];
			for (int i = 0; i < scalingVector.length; i++) {
				double value = data.costVector[i] * scalingVector[i];
				directCosts[i] = value;
				tCosts += value;
			}
			totalCosts = tCosts;
		}
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
	public FlowIndex flowIndex() {
		return data.flowIndex;
	}

	@Override
	public DIndex<ImpactCategoryDescriptor> impactIndex() {
		return data.impactIndex;
	}

	@Override
	public double[] scalingVector() {
		return scalingVector;
	}

	@Override
	public double scalingFactorOf(int product) {
		return scalingVector[product];
	}

	@Override
	public double[] totalRequirements() {
		if (totalRequirements != null)
			return totalRequirements;
		var t = data.techMatrix.diag();
		for (int i = 0; i < t.length; i++) {
			t[i] *= scalingVector[i];
		}
		totalRequirements = t;
		return t;
	}

	@Override
	public double totalRequirementsOf(int product) {
		return totalRequirements()[product];
	}

	@Override
	public double[] techColumnOf(int j) {
		return data.techMatrix.getColumn(j);
	}

	@Override
	public double techValueOf(int row, int col) {
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
	public double loopFactorOf(int product) {
		var aii = data.techMatrix.get(product, product);
		var eii = solutionOfOne(product)[product];
		var f = aii * eii;
		return f == 0
				? 0
				: 1 / f;
	}

	@Override
	public double[] unscaledFlowsOf(int product) {
		return data.flowMatrix.getColumn(product);
	}

	@Override
	public double unscaledFlowOf(int flow, int product) {
		return data.flowMatrix.get(flow, product);
	}

	private IMatrix directFlows() {
		if (directFlows != null)
			return directFlows;
		if (data.flowMatrix == null)
			return null;
		var m = data.flowMatrix.copy();
		m.scaleColumns(scalingVector);
		directFlows = m;
		return directFlows;
	}

	@Override
	public double[] directFlowsOf(int product) {
		var m = directFlows();
		return m == null
				? EMPTY_VECTOR
				: m.getColumn(product);
	}

	@Override
	public double directFlowOf(int flow, int product) {
		var m = directFlows();
		return m == null
				? 0
				: m.get(flow, product);
	}

	@Override
	public double[] totalFlowsOfOne(int product) {
		if (totalFlowsOfOne == null)
			return EMPTY_VECTOR;
		var totals = totalFlowsOfOne.get(product);
		if (totals != null)
			return totals;
		var s = solutionOfOne(product);
		totals = solver.multiply(data.flowMatrix, s);
		totalFlowsOfOne.put(product, totals);
		return totals;
	}

	@Override
	public double[] totalFlows() {
		return totalFlows == null
				? EMPTY_VECTOR
				: totalFlows;
	}

	@Override
	public double[] impactFactorsOf(int flow) {
		return data.impactMatrix == null
				? new double[0]
				: data.impactMatrix.getColumn(flow);
	}

	@Override
	public double impactFactorOf(int indicator, int flow) {
		return data.impactMatrix == null
				? 0
				: data.impactMatrix.get(indicator, flow);
	}

	@Override
	public double[] flowImpactsOf(int flow) {
		if (totalFlows == null)
			return new double[0];
		var impacts = impactFactorsOf(flow);
		scaleInPlace(impacts, totalFlows[flow]);
		return impacts;
	}

	@Override
	public double flowImpactOf(int indicator, int flow) {
		if (totalFlows == null)
			return 0;
		return totalFlows[flow] * impactFactorOf(indicator, flow);
	}

	private IMatrix directImpacts() {
		if (directImpacts != null)
			return directImpacts;
		if (data.impactMatrix == null)
			return null;
		var flows = directFlows();
		if (flows == null)
			return null;
		directImpacts = solver.multiply(data.impactMatrix, flows);
		return directImpacts;
	}

	@Override
	public double[] directImpactsOf(int product) {
		var impacts = directImpacts();
		return impacts == null
				? new double[0]
				: impacts.getColumn(product);
	}

	@Override
	public double directImpactOf(int indicator, int product) {
		var impacts = directImpacts();
		return impacts == null
				? 0
				: impacts.get(indicator, product);
	}

	@Override
	public double[] totalImpactsOfOne(int product) {
		if (totalImpactsOfOne == null)
			return EMPTY_VECTOR;
		var h = totalImpactsOfOne.get(product);
		if (h != null)
			return h;
		var g = totalFlowsOfOne(product);
		h = solver.multiply(data.impactMatrix, g);
		totalImpactsOfOne.put(product, h);
		return h;
	}

	@Override
	public double[] totalImpacts() {
		return totalImpacts == null
				? new double[0]
				: totalImpacts;
	}

	@Override
	public double directCostsOf(int product) {
		return directCosts == null
				? 0
				: directCosts[product];
	}

	@Override
	public double totalCostsOfOne(int product) {
		if (data.costVector == null)
			return 0;
		var s = solutionOfOne(product);
		double c = 0.0;
		for (int j = 0; j < s.length; j++) {
			c += s[j] * data.costVector[j];
		}
		return c;
	}

	@Override
	public double totalCosts() {
		return totalCosts;
	}

}
