package org.openlca.core.results.solutions;

import org.openlca.core.matrix.DIndex;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public class EagerSolutionProvider implements SolutionProvider {

	private final MatrixData data;

	private final IMatrix inverse;
	private final double[] scalingVector;
	private final double[] totalRequirements;

	private double[] totalFlows;
	private double[] totalImpacts;
	private double totalCosts;


	private IMatrix directFlowResults;
	private IMatrix flowIntensities;

	private IMatrix directImpacts;
	private IMatrix impactIntensities;
	private double[] costIntensities;

	private EagerSolutionProvider(MatrixData data, IMatrixSolver solver) {
		this.data = data;

		// product and waste flows flows
		var techIdx = data.techIndex;
		inverse = solver.invert(data.techMatrix);
		var refIdx = techIdx.getIndex(techIdx.getRefFlow());
		scalingVector = inverse.getColumn(refIdx);
		var demand = techIdx.getDemand();
		for (int i = 0; i < scalingVector.length; i++) {
			scalingVector[i] *= demand;
		}
		totalRequirements = data.techMatrix.diag();
		for (int i = 0; i < totalRequirements.length; i++) {
			totalRequirements[i] *= scalingVector[i];
		}

	}

	public static EagerSolutionProvider create(
			MatrixData data,
			IMatrixSolver solver) {
		var provider = new EagerSolutionProvider(data);

		if (data.flowMatrix != null) {

			provider.directFlowResults = data.flowMatrix.copy();
			provider.directFlowResults.scaleColumns(provider.scalingVector());

			// the intensity matrix: M = B * inv(A)
			provider.flowIntensities = solver.multiply(
					data.flowMatrix,
					provider.inverse);
			provider.totalFlows = provider.totalFlowResultsOfOne(refIdx);
			for (int i = 0; i < provider.totalFlows.length; i++) {
				provider.totalFlows[i] *= demand;
			}

			if (data.impactMatrix != null) {

				provider.directImpacts = solver.matrix(
						data.impactMatrix,
						data.
				)

				// impacts of the intensities: C * M
				provider.impactIntensities = solver.multiply(
						data.impactMatrix,
						provider.flowIntensities);
				provider.totalImpacts = provider.totalImpactsOfOne(refIdx);
				for(int i = 0; i < provider.totalImpacts.length; i++) {
					provider.totalImpacts[i] *= demand;
				}
			}
		}

		if (data.costVector != null) {
			var n = data.costVector.length;
			var costs = solver.matrix(1, n);
			for (int col = 0; col < n; col++) {
				costs.set(0, col, data.costVector[col]);
			}
			provider.costIntensities = solver.multiply(
					costs, provider.inverse)
					.getRow(0);
			provider.totalCosts = provider.totalCostsOfOne(refIdx) * demand;
		}

		return provider;
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
	public double[] totalRequirements() {
		return totalRequirements;
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
		return inverse.getColumn(product);
	}

	@Override
	public double[] flowColumnOf(int j) {
		return data.flowMatrix.getColumn(j);
	}

	@Override
	public double flowValueOf(int flow, int product) {
		return data.flowMatrix.get(flow, product);
	}

	@Override
	public double[] totalFlowResults() {
		return totalFlows == null
				? new double[0]
				: totalFlows;
	}

	@Override
	public double[] totalFlowResultsOfOne(int product) {
		if (flowIntensities == null)
			return new double[0];
		return flowIntensities.getColumn(product);
	}

	@Override
	public double totalFlowResultOfOne(int flow, int product) {
		if (flowIntensities == null)
			return 0;
		return flowIntensities.get(flow, product);
	}

	@Override
	public double[] totalImpacts() {
		return totalImpacts == null
				? new double[0]
				: totalImpacts;
	}

	@Override
	public double[] totalImpactsOfOne(int product) {
		if (impactIntensities == null)
			return new double[0];
		return impactIntensities.getColumn(product);
	}

	@Override
	public double totalImpactOfOne(int indicator, int product) {
		if (impactIntensities == null)
			return 0;
		return impactIntensities.get(indicator, product);
	}

	@Override
	public double totalCosts() {
		return totalCosts;
	}

	@Override
	public double totalCostsOfOne(int product) {
		if (costIntensities == null)
			return 0;
		return costIntensities[product];
	}

	@Override
	public double loopFactorOf(int i) {
		var aii = data.techMatrix.get(i, i);
		var ii = inverse.get(i, i);
		var f = aii * ii;
		return f == 0
				? 0
				: 1 / f;
	}
}
