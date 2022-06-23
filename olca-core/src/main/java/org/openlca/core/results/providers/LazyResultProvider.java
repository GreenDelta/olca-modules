package org.openlca.core.results.providers;

import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.solvers.Factorization;
import org.openlca.core.matrix.solvers.MatrixSolver;

import gnu.trove.map.hash.TIntObjectHashMap;

public class LazyResultProvider implements ResultProvider {

	private final Demand demand;
	private final MatrixData data;
	private final MatrixSolver solver;
	private final Factorization factorization;

	private final double[] scalingVector;
	private double[] totalRequirements;
	private final TIntObjectHashMap<double[]> solutions;

	private Matrix directFlows;
	private final double[] totalFlows;
	private final TIntObjectHashMap<double[]> totalFlowsOfOne;

	private Matrix directImpacts;
	private double[] totalImpacts;
	private final TIntObjectHashMap<double[]> totalImpactsOfOne;

	private final double[] directCosts;
	private final double totalCosts;

	private LazyResultProvider(SolverContext context) {
		this.demand = context.demand();
		this.data = context.data();
		this.solver = context.solver();
		this.factorization = solver.factorize(data.techMatrix);

		solutions = new TIntObjectHashMap<>();
		totalFlowsOfOne = hasFlows()
			? new TIntObjectHashMap<>()
			: null;
		totalImpactsOfOne = hasImpacts()
			? new TIntObjectHashMap<>()
			: null;

		// calculate the scaling vector
		var refIdx = data.techIndex.of(demand.techFlow());
		var s = solutionOfOne(refIdx);
		var d = demand.value();
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

	public static LazyResultProvider create(SolverContext context) {
		return new LazyResultProvider(context);
	}

	@Override
	public Demand demand() {
		return demand;
	}

	@Override
	public TechIndex techIndex() {
		return data.techIndex;
	}

	@Override
	public EnviIndex enviIndex() {
		return data.enviIndex;
	}

	@Override
	public ImpactIndex impactIndex() {
		return data.impactIndex;
	}

	@Override
	public boolean hasCosts() {
		return !isEmpty(data.costVector);
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
	public double totalRequirementsOf(int techFlow) {
		return totalRequirements()[techFlow];
	}

	@Override
	public double[] techColumnOf(int techFlow) {
		return data.techMatrix.getColumn(techFlow);
	}

	@Override
	public double techValueOf(int row, int col) {
		return data.techMatrix.get(row, col);
	}

	@Override
	public double[] solutionOfOne(int techFlow) {
		var s = solutions.get(techFlow);
		if (s != null)
			return s;
		s = factorization.solve(techFlow, 1.0);
		solutions.put(techFlow, s);
		return s;
	}

	@Override
	public double loopFactorOf(int techFlow) {
		var aii = data.techMatrix.get(techFlow, techFlow);
		var eii = solutionOfOne(techFlow)[techFlow];
		var f = aii * eii;
		return f == 0
			? 0
			: 1 / f;
	}

	@Override
	public double[] unscaledFlowsOf(int techFlow) {
		return data.enviMatrix.getColumn(techFlow);
	}

	@Override
	public double unscaledFlowOf(int flow, int product) {
		return data.enviMatrix.get(flow, product);
	}

	private Matrix directFlows() {
		if (directFlows != null)
			return directFlows;
		if (data.enviMatrix == null)
			return null;
		var m = data.enviMatrix.asMutableCopy();
		m.scaleColumns(scalingVector);
		directFlows = m;
		return directFlows;
	}

	@Override
	public double[] directFlowsOf(int techFlow) {
		var m = directFlows();
		return m != null
			? m.getColumn(techFlow)
			: new double[enviIndex().size()];
	}

	@Override
	public double directFlowOf(int flow, int techFlow) {
		var m = directFlows();
		return m != null
			? m.get(flow, techFlow)
			: 0;
	}

	@Override
	public double[] totalFlowsOfOne(int techFlow) {
		if (totalFlowsOfOne == null)
			return EMPTY_VECTOR;
		var totals = totalFlowsOfOne.get(techFlow);
		if (totals != null)
			return totals;
		var s = solutionOfOne(techFlow);
		totals = solver.multiply(data.enviMatrix, s);
		totalFlowsOfOne.put(techFlow, totals);
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
		return data.impactMatrix != null
			? data.impactMatrix.getColumn(flow)
			: new double[impactIndex().size()];
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
			return EMPTY_VECTOR;
		var impacts = impactFactorsOf(flow);
		scaleInPlace(impacts, totalFlows[flow]);
		return impacts;
	}

	@Override
	public double flowImpactOf(int indicator, int flow) {
		return totalFlows != null
			? totalFlows[flow] * impactFactorOf(indicator, flow)
			: 0;
	}

	@Override
	public double[] directImpactsOf(int techFlow) {
		var impacts = directImpacts();
		return impacts != null
			? impacts.getColumn(techFlow)
			: new double[impactIndex().size()];
	}

	@Override
	public double directImpactOf(int indicator, int techFlow) {
		var impacts = directImpacts();
		return impacts != null
			? impacts.get(indicator, techFlow)
			: 0;
	}

	private Matrix directImpacts() {
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
	public double[] totalImpactsOfOne(int techFlow) {
		if (totalImpactsOfOne == null)
			return EMPTY_VECTOR;
		var h = totalImpactsOfOne.get(techFlow);
		if (h != null)
			return h;
		var impactFactors = data.impactMatrix;
		var g = totalFlowsOfOne(techFlow);
		if (impactFactors == null || g.length == 0)
			return new double[impactIndex().size()];
		h = solver.multiply(impactFactors, g);
		totalImpactsOfOne.put(techFlow, h);
		return h;
	}

	@Override
	public double[] totalImpacts() {
		if (totalImpacts != null)
			return totalImpacts;
		if (!hasImpacts())
			return EMPTY_VECTOR;
		totalImpacts = new double[impactIndex().size()];
		return totalImpacts;
	}

	@Override
	public double directCostsOf(int techFlow) {
		return directCosts == null
			? 0
			: directCosts[techFlow];
	}

	@Override
	public double totalCostsOfOne(int techFlow) {
		if (data.costVector == null)
			return 0;
		var s = solutionOfOne(techFlow);
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
