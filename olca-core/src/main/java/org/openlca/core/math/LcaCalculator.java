package org.openlca.core.math;

import org.openlca.core.matrix.ImpactMatrix;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.LSAResult;
import org.openlca.core.results.LinkContributions;
import org.openlca.core.results.SimpleResult;

public class LcaCalculator {

	private final IMatrixSolver solver;

	public LcaCalculator(IMatrixSolver solver) {
		this.solver = solver;
	}

	public SimpleResult calculateSimple(InventoryMatrix matrix) {
		return calculateSimple(matrix, null);
	}

	public SimpleResult calculateSimple(InventoryMatrix matrix,
			ImpactMatrix impactMatrix) {

		SimpleResult result = new SimpleResult();
		result.setFlowIndex(matrix.getFlowIndex());
		result.setProductIndex(matrix.getProductIndex());

		IMatrix techMatrix = matrix.getTechnologyMatrix();
		ProductIndex productIndex = matrix.getProductIndex();
		int idx = productIndex.getIndex(productIndex.getRefProduct());
		double[] s = solver.solve(techMatrix, idx, productIndex.getDemand());
		IMatrix enviMatrix = matrix.getInterventionMatrix();

		double[] g = solver.multiply(enviMatrix, s);
		result.setTotalFlowResults(g);

		if (impactMatrix != null) {
			IMatrix impactFactors = impactMatrix.getFactorMatrix();
			double[] i = solver.multiply(impactFactors, g);
			result.setImpactIndex(impactMatrix.getCategoryIndex());
			result.setTotalImpactResults(i);
		}
		return result;
	}

	public ContributionResult calculateContributions(InventoryMatrix matrix) {
		return calculateContributions(matrix, null);
	}

	public ContributionResult calculateContributions(InventoryMatrix matrix,
			ImpactMatrix impactMatrix) {
		return calculateContributions(new ContributionResult(), matrix,
				impactMatrix);
	}

	public <T extends ContributionResult> T calculateContributions(T result,
			InventoryMatrix matrix, ImpactMatrix impactMatrix) {

		result.setFlowIndex(matrix.getFlowIndex());
		result.setProductIndex(matrix.getProductIndex());

		IMatrix techMatrix = matrix.getTechnologyMatrix();
		ProductIndex productIndex = matrix.getProductIndex();
		int idx = productIndex.getIndex(productIndex.getRefProduct());
		double[] s = solver.solve(techMatrix, idx, productIndex.getDemand());
		result.setScalingFactors(s);

		IMatrix enviMatrix = matrix.getInterventionMatrix();
		IMatrix singleResult = enviMatrix.copy();
		solver.scaleColumns(singleResult, s);
		result.setSingleFlowResults(singleResult);
		double[] g = solver.multiply(enviMatrix, s);
		result.setTotalFlowResults(g);

		LinkContributions linkContributions = LinkContributions.calculate(
				techMatrix, productIndex, s);
		result.setLinkContributions(linkContributions);

		if (impactMatrix != null) {
			IMatrix impactFactors = impactMatrix.getFactorMatrix();
			double[] i = solver.multiply(impactFactors, g);
			result.setImpactIndex(impactMatrix.getCategoryIndex());
			result.setTotalImpactResults(i);
			IMatrix singleImpactResult = solver.multiply(impactFactors,
					singleResult);
			result.setSingleImpactResults(singleImpactResult);

			IMatrix singleFlowImpacts = impactFactors.copy();
			solver.scaleColumns(singleFlowImpacts, g);
			result.setSingleFlowImpacts(singleFlowImpacts);

		}
		return result;
	}

	/**
	 * Computes a local sensitivity analysis according to (Heijungs, 2010).
	 *  Heijungs, R. Sensitivity coefficients for matrix-based LCA. Int. J.
	 *  Life Cycle Assess. 2010, 15, 511−520.
	 * @param matrix
	 * @param impactMatrix
	 * @return
	 */
	public LSAResult calculateLSA(InventoryMatrix matrix,
			ImpactMatrix impactMatrix) {
		LSAResult result = new LSAResult();
		calculateContributions(result, matrix, impactMatrix);

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
		IMatrix[] rscA = new IMatrix[Q.getRowDimension()];
		// and of the environmental matrix B
		IMatrix[] rscB = new IMatrix[Q.getRowDimension()];
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
			rscA[k] = rsc;
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
			rscB[k] = rsc;
		}
		result.setRscA(rscA);
		result.setRscB(rscB);
		return result;
	}

	public FullResult calculateFull(InventoryMatrix matrix) {
		return calculateFull(matrix, null);
	}

	public FullResult calculateFull(InventoryMatrix matrix,
			ImpactMatrix impactMatrix) {

		FullResult result = new FullResult();
		result.setFlowIndex(matrix.getFlowIndex());
		result.setProductIndex(matrix.getProductIndex());

		ProductIndex productIndex = matrix.getProductIndex();
		IMatrix techMatrix = matrix.getTechnologyMatrix();
		IMatrix enviMatrix = matrix.getInterventionMatrix();
		IMatrix inverse = solver.invert(techMatrix);
		double[] scalingVector = getScalingVector(inverse, productIndex);
		result.setScalingFactors(scalingVector);

		// single results
		IMatrix singleResult = enviMatrix.copy();
		solver.scaleColumns(singleResult, scalingVector);
		result.setSingleFlowResults(singleResult);

		// total results
		double[] demands = new double[productIndex.size()];
		for (int i = 0; i < productIndex.size(); i++) {
			double entry = techMatrix.getEntry(i, i);
			double s = scalingVector[i];
			demands[i] = s * entry;
		}
		IMatrix totalResult = solver.multiply(enviMatrix, inverse);

		// allow GC
		inverse = null;

		solver.scaleColumns(totalResult, demands);
		result.setUpstreamFlowResults(totalResult);
		int refIdx = productIndex.getIndex(productIndex.getRefProduct());
		double[] g = totalResult.getColumn(refIdx);
		result.setTotalFlowResults(g);

		LinkContributions linkContributions = LinkContributions.calculate(
				techMatrix, productIndex, scalingVector);
		result.setLinkContributions(linkContributions);

		if (impactMatrix != null) {
			result.setImpactIndex(impactMatrix.getCategoryIndex());
			IMatrix factors = impactMatrix.getFactorMatrix();
			IMatrix singleImpactResult = solver.multiply(factors, singleResult);
			result.setSingleImpactResults(singleImpactResult);

			IMatrix singleFlowImpacts = factors.copy();
			solver.scaleColumns(singleFlowImpacts, g);
			result.setSingleFlowImpacts(singleFlowImpacts);

			IMatrix totalImpactResult = solver.multiply(factors, totalResult);
			result.setUpstreamImpactResults(totalImpactResult);
			result.setTotalImpactResults(totalImpactResult.getColumn(refIdx));
		}
		return result;

	}

	private double[] getScalingVector(IMatrix inverse, ProductIndex productIndex) {
		LongPair refProduct = productIndex.getRefProduct();
		int idx = productIndex.getIndex(refProduct);
		double[] s = inverse.getColumn(idx);
		double demand = productIndex.getDemand();
		for (int i = 0; i < s.length; i++)
			s[i] *= demand;
		return s;
	}

}
