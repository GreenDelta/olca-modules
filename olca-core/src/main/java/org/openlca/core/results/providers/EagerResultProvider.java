package org.openlca.core.results.providers;

import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechIndex;

public class EagerResultProvider implements ResultProvider {

	private final MatrixData data;

	private final Matrix inverse;
	private final double[] scalingVector;
	private final double[] totalRequirements;
	private final double[] loopFactors;

	private double[] totalFlows;
	private Matrix directFlows;
	private Matrix totalFlowsOfOne;

	private double[] totalImpacts;
	private Matrix directImpacts;
	private Matrix totalImpactsOfOne;

	private double totalCosts;
	private double[] directCosts;
	private double[] totalCostsOfOne;

	private EagerResultProvider(SolverContext context) {
		this.data = context.data();

		// product and waste flows
		var techIdx = data.techIndex;
		var solver = context.solver();
		inverse = solver.invert(data.techMatrix);
		var refIdx = techIdx.of(data.demand.techFlow());
		scalingVector = inverse.getColumn(refIdx);
		var demand = data.demand.value();
		for (int i = 0; i < scalingVector.length; i++) {
			scalingVector[i] *= demand;
		}
		totalRequirements = data.techMatrix.diag();
		for (int i = 0; i < totalRequirements.length; i++) {
			totalRequirements[i] *= scalingVector[i];
		}
		loopFactors = new double[techIdx.size()];
		for (int i = 0; i < loopFactors.length; i++) {
			var aii = data.techMatrix.get(i, i);
			var ii = inverse.get(i, i);
			var f = aii * ii;
			loopFactors[i] = f == 0
				? 1.0
				: 1 / f;
		}

		if (data.enviMatrix != null) {

			// elementary flows
			directFlows = data.enviMatrix.asMutableCopy();
			directFlows.scaleColumns(scalingVector);

			// the intensity matrix: M = B * inv(A)
			totalFlowsOfOne = solver.multiply(data.enviMatrix, inverse);
			totalFlows = totalFlowsOfOne(refIdx);
			for (int i = 0; i < totalFlows.length; i++) {
				totalFlows[i] *= demand;
			}

			if (data.impactMatrix != null) {

				directImpacts = solver.multiply(
					data.impactMatrix,
					directFlows);
				totalImpactsOfOne = solver.multiply(
					data.impactMatrix,
					totalFlowsOfOne);
				totalImpacts = totalImpactsOfOne(refIdx);
				for (int i = 0; i < totalImpacts.length; i++) {
					totalImpacts[i] *= demand;
				}
			}
		}

		// costs
		if (data.costVector != null) {
			var n = data.costVector.length;
			directCosts = new double[n];
			var costMatrix = solver.matrix(1, n);
			for (int j = 0; j < n; j++) {
				var costs = data.costVector[j];
				directCosts[j] = costs * scalingVector[j];
				costMatrix.set(0, j, costs);
			}
			totalCostsOfOne = solver.multiply(costMatrix, inverse).getRow(0);
			totalCosts = totalCostsOfOne(refIdx) * demand;
		}
	}

	public static EagerResultProvider create(SolverContext context) {
		return new EagerResultProvider(context);
	}

	@Override
	public Demand demand() {
		return data.demand;
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
		return totalRequirements;
	}

	@Override
	public double totalRequirementsOf(int techFlow) {
		return totalRequirements[techFlow];
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
		return inverse.getColumn(techFlow);
	}

	@Override
	public double loopFactorOf(int techFlow) {
		return loopFactors[techFlow];
	}

	@Override
	public double[] unscaledFlowsOf(int techFlow) {
		return data.enviMatrix == null
			? EMPTY_VECTOR
			: data.enviMatrix.getColumn(techFlow);
	}

	@Override
	public double unscaledFlowOf(int flow, int product) {
		return data.enviMatrix == null
			? 0
			: data.enviMatrix.get(flow, product);
	}

	@Override
	public double[] directFlowsOf(int techFlow) {
		return directFlows == null
			? EMPTY_VECTOR
			: directFlows.getColumn(techFlow);
	}

	@Override
	public double directFlowOf(int flow, int techFlow) {
		return directFlows == null
			? 0
			: directFlows.get(flow, techFlow);
	}

	@Override
	public double[] totalFlowsOfOne(int techFlow) {
		return totalFlowsOfOne == null
			? EMPTY_VECTOR
			: totalFlowsOfOne.getColumn(techFlow);
	}

	@Override
	public double totalFlowOfOne(int flow, int techFlow) {
		return totalFlowsOfOne == null
			? 0
			: totalFlowsOfOne.get(flow, techFlow);
	}

	@Override
	public double[] totalFlowsOf(int techFlow) {
		var factor = totalFactorOf(techFlow);
		var totals = totalFlowsOfOne(techFlow);
		scaleInPlace(totals, factor);
		return totals;
	}

	@Override
	public double[] totalFlows() {
		if (totalFlows != null)
			return totalFlows;
		if (!hasFlows())
			return EMPTY_VECTOR;
		totalFlows = new double[enviIndex().size()];
		return totalFlows;
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
			return new double[impactIndex().size()];
		var total = totalFlows[flow];
		var impacts = impactFactorsOf(flow);
		scaleInPlace(impacts, total);
		return impacts;
	}

	@Override
	public double flowImpactOf(int indicator, int flow) {
		if (totalFlows == null)
			return 0;
		var total = totalFlows[flow];
		return total * impactFactorOf(indicator, flow);
	}

	@Override
	public double[] directImpactsOf(int techFlow) {
		return directImpacts != null
			? directImpacts.getColumn(techFlow)
			: new double[impactIndex().size()];
	}

	@Override
	public double directImpactOf(int indicator, int techFlow) {
		return directImpacts == null
			? 0
			: directImpacts.get(indicator, techFlow);
	}


	@Override
	public double[] totalImpactsOfOne(int techFlow) {
		return totalImpactsOfOne != null
			? totalImpactsOfOne.getColumn(techFlow)
			: new double[impactIndex().size()];
	}

	@Override
	public double totalImpactOfOne(int indicator, int techFlow) {
		return totalImpactsOfOne == null
			? 0
			: totalImpactsOfOne.get(indicator, techFlow);
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
		return totalCostsOfOne == null
			? 0
			: totalCostsOfOne[techFlow];
	}

	@Override
	public double totalCosts() {
		return totalCosts;
	}
}
