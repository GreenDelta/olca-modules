package org.openlca.core.results.providers;

import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.solvers.MatrixSolver;

public record InversionResult(
	MatrixData data,
	MatrixReader inverse,
	double[] scalingVector,
	double[] totalRequirements,
	double[] loopFactors,
	double[] totalFlows,
	Matrix directFlows,
	MatrixReader flowIntensities,
	double[] totalImpacts,
	MatrixReader directImpacts,
	MatrixReader impactIntensities,
	double totalCosts,
	double[] directCosts,
	double[] costIntensities
) {

	public static Calculator of(MatrixSolver solver, MatrixData data) {
		return new Calculator(solver, data);
	}

	public static Calculator of(SolverContext context) {
		return new Calculator(context.solver(), context.data());
	}

	public ResultProvider provider() {
		return InversionResultProvider.of(this);
	}

	public static class Calculator {
		private final MatrixSolver solver;
		private final MatrixData data;
		private MatrixReader inverse;
		private MatrixReader flowIntensities;

		private Calculator(MatrixSolver solver, MatrixData data) {
			this.solver = solver;
			this.data = data;
		}

		public Calculator withInverse(MatrixReader inv) {
			this.inverse = inv;
			return this;
		}

		public Calculator withFlowIntensities(MatrixReader m) {
			this.flowIntensities = m;
			return this;
		}

		public InversionResult calculate() {

			var techIdx = data.techIndex;
			var refIdx = techIdx.of(data.demand.techFlow());
			var inverse = this.inverse != null
				? this.inverse
				: solver.invert(data.techMatrix);

			// calculate the scaling vector
			var scalingVector = inverse.getColumn(refIdx);
			var demandValue = data.demand.value();
			for (int i = 0; i < scalingVector.length; i++) {
				scalingVector[i] *= demandValue;
			}

			// total requirements
			var totalRequirements = data.techMatrix.diag();
			for (int i = 0; i < totalRequirements.length; i++) {
				totalRequirements[i] *= scalingVector[i];
			}

			// loop factors
			var loopFactors = new double[techIdx.size()];
			for (int i = 0; i < loopFactors.length; i++) {
				var aii = data.techMatrix.get(i, i);
				var ii = inverse.get(i, i);
				var f = aii * ii;
				loopFactors[i] = f == 0
					? 1.0
					: 1 / f;
			}

			// flow results
			Matrix directFlows = null;
			MatrixReader flowIntensities = null;
			double[] totalFlows = null;
			if (data.enviMatrix != null) {

				directFlows = data.enviMatrix.asMutableCopy();
				directFlows.scaleColumns(scalingVector);
				flowIntensities = this.flowIntensities != null
					? this.flowIntensities
					: solver.multiply(data.enviMatrix, inverse);

				totalFlows = flowIntensities.getColumn(refIdx);
				for (int i = 0; i < totalFlows.length; i++) {
					totalFlows[i] *= demandValue;
				}
			}

			// impact assessment results
			Matrix directImpacts = null;
			MatrixReader impactIntensities = null;
			double[] totalImpacts = null;
			if (data.impactMatrix != null && flowIntensities != null) {
				directImpacts = solver.multiply(
					data.impactMatrix, directFlows);
				impactIntensities = solver.multiply(
					data.impactMatrix, flowIntensities);
				totalImpacts = impactIntensities.getColumn(refIdx);
				for (int i = 0; i < totalImpacts.length; i++) {
					totalImpacts[i] *= demandValue;
				}
			}

			// costs
			double[] directCosts = null;
			double[] costIntensities = null;
			double totalCosts = 0;
			if (data.costVector != null) {
				var n = data.costVector.length;
				directCosts = new double[n];
				var costMatrix = solver.matrix(1, n);
				for (int j = 0; j < n; j++) {
					var costs = data.costVector[j];
					directCosts[j] = costs * scalingVector[j];
					costMatrix.set(0, j, costs);
				}
				costIntensities = solver.multiply(costMatrix, inverse).getRow(0);
				totalCosts = costIntensities[refIdx] * demandValue;
			}

			return new InversionResult(
				data,
				inverse,
				scalingVector,
				totalRequirements,
				loopFactors,
				totalFlows,
				directFlows,
				flowIntensities,
				totalImpacts,
				directImpacts,
				impactIntensities,
				totalCosts,
				directCosts,
				costIntensities
			);
		}
	}
}
