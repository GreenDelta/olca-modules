package org.openlca.core.math;

import org.openlca.core.matrix.ImpactMatrix;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.results.LocalSensitivityResult;

public class SensitivityCalculator {
	private final IMatrixSolver solver;
	private final LcaCalculator lcaCalculator;

	public SensitivityCalculator(IMatrixSolver solver) {
		super();
		this.solver = solver;
		this.lcaCalculator = new LcaCalculator(solver);
	}

	/**
	 * Computes a local sensitivity analysis according to (Heijungs, 2010).
	 *  Heijungs, R. Sensitivity coefficients for matrix-based LCA. Int. J.
	 *  Life Cycle Assess. 2010, 15, 511−520.
	 * @param matrix
	 * @param impactMatrix
	 * @return
	 */
	public LocalSensitivityResult calculateLocalResult(InventoryMatrix matrix,
			ImpactMatrix impactMatrix) {
		LocalSensitivityResult result = new LocalSensitivityResult();
		lcaCalculator.calculateContributions(result, matrix, impactMatrix);

		IMatrix techMatrix = matrix.getTechnologyMatrix();
		IMatrix enviMatrix = matrix.getInterventionMatrix();
		double[] s = result.getScalingFactors();
		// we want to solve X.A = B, equivalent to tA.tX = tB
		IMatrix lambda = solver.transpose(solver.solve(
				solver.transpose(techMatrix), solver.transpose(enviMatrix)));
		IMatrix Q = impactMatrix.getFactorMatrix();
		double[] h = result.getTotalImpactResults();

		// Compute relative sensitity coefficient (RSC)
		// of the economic matrix A
		IMatrix[] techCoefficients = new IMatrix[Q.getRowDimension()];
		// and of the environmental matrix B
		IMatrix[] enviCoefficients = new IMatrix[Q.getRowDimension()];
		IMatrix QL = solver.multiply(Q, lambda);
		// for each impact
		for (int k = 0; k < Q.getRowDimension(); k++) {
			IMatrix rsc = solver.getMatrixFactory().create(
					techMatrix.getRowDimension(),
					techMatrix.getColumnDimension());
			double hk = h[k];
			// for each flow
			for (int i = 0; i < rsc.getRowDimension(); i++) {
				double qlki = QL.getEntry(k, i);
				for (int j = 0; j < rsc.getColumnDimension(); j++) {
					rsc.setEntry(i, j, -s[j] * qlki * techMatrix.getEntry(i, j)
							/ hk);
				}
			}
			techCoefficients[k] = rsc;
			// now, for the env matrix
			rsc = solver.getMatrixFactory().create(
					enviMatrix.getRowDimension(),
					enviMatrix.getColumnDimension());
			// for each flow
			for (int i = 0; i < enviMatrix.getRowDimension(); i++) {
				for (int j = 0; j < enviMatrix.getColumnDimension(); j++) {
					double qki = Q.getEntry(k, i);
					rsc.setEntry(i, j, s[j] * qki * enviMatrix.getEntry(i, j)
							/ hk);
				}
			}
			enviCoefficients[k] = rsc;
		}
		result.setTechCoefficients(techCoefficients);
		result.setEnviCoefficients(enviCoefficients);
		return result;
	}


}
