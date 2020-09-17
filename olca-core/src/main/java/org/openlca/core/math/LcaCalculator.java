package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;
import org.openlca.core.results.solutions.EagerSolutionProvider;
import org.openlca.core.results.solutions.LazySolutionProvider;
import org.openlca.core.results.solutions.LibrarySolutionProvider;
import org.openlca.core.results.solutions.SolutionProvider;

/**
 * This calculator does the low level matrix based LCA-calculation. Typically,
 * you do not want to use this directly but a more high level calculator where
 * you can directly throw in a calculation setup or project.
 */
public class LcaCalculator {

	private final IMatrixSolver solver;
	private final MatrixData data;
	private IDatabase db;
	private LibraryDir libDir;

	public LcaCalculator(IMatrixSolver solver, MatrixData data) {
		this.solver = solver;
		this.data = data;
		this.data.compress();
	}

	public LcaCalculator withLibraries(IDatabase db, LibraryDir libDir) {
		this.db = db;
		this.libDir = libDir;
		return this;
	}

	private SolutionProvider solution(boolean forceLazy) {
		if (db != null && libDir != null)
			return LibrarySolutionProvider.of(db, libDir, solver, data);
		if (forceLazy)
			return LazySolutionProvider.create(data, solver);
		// TODO: consider the matrix size and format and the capabilities
		// of the solver...
		return data.isSparse()
				? LazySolutionProvider.create(data, solver)
				: EagerSolutionProvider.create(data, solver);
	}

	public SimpleResult calculateSimple() {
		var solution = solution(true);
		var result = new SimpleResult();
		fillSimple(result, solution);
		return result;
	}

	private void fillSimple(SimpleResult r, SolutionProvider s) {
		r.techIndex = s.techIndex();
		r.flowIndex = s.flowIndex();
		r.impactIndex = s.impactIndex();
		r.scalingVector = s.scalingVector();
		r.totalRequirements = s.totalRequirements();

		if (r.flowIndex != null && !r.flowIndex.isEmpty()) {
			r.totalFlowResults = s.totalFlowResults();
			if (r.impactIndex != null && !r.impactIndex.isEmpty()) {
				r.totalImpactResults = s.totalImpacts();
			}
		}
		r.totalCosts = s.totalCosts();
	}

	public ContributionResult calculateContributions() {

		var solution = solution(true);
		var result = new ContributionResult(solution);
		fillSimple(result, solution);

		IMatrix techMatrix = data.techMatrix;
		TechIndex productIndex = data.techIndex;
		int idx = productIndex.getIndex(productIndex.getRefFlow());
		double[] s = solver.solve(techMatrix, idx, productIndex.getDemand());

		IMatrix enviMatrix = data.enviMatrix;
		IMatrix singleResult = enviMatrix.copy();
		singleResult.scaleColumns(s);
		result.directFlowResults = singleResult;

		if (data.impactMatrix != null) {
			addDirectImpacts(result);
		}

		if (data.costVector != null) {
			addDirectCosts(result, s);
		}
		return result;
	}

	public FullResult calculateFull() {
		var solution = solution(false);
		var result = new FullResult(solution);

		double[] scalingVector = solution.scalingVector();

		// flow results
		IMatrix enviMatrix = data.enviMatrix;
		if (enviMatrix != null) {
			result.directFlowResults = enviMatrix.copy();
			result.directFlowResults.scaleColumns(scalingVector);
		}

		if (data.impactMatrix != null) {
			addDirectImpacts(result);
		}

		if (data.costVector != null) {
			addDirectCosts(result, scalingVector);
		}
		return result;
	}


	/**
	 * Calculates the scaling vector for the reference product i from the given
	 * inverse of the technology matrix:
	 * <p>
	 * s = d[i] .* Inverse[:, i]
	 * <p>
	 * where d is the demand vector and.
	 */
	public double[] getScalingVector(IMatrix inverse, TechIndex techIndex) {
		ProcessProduct refProduct = techIndex.getRefFlow();
		int idx = techIndex.getIndex(refProduct);
		double[] s = inverse.getColumn(idx);
		double demand = techIndex.getDemand();
		for (int i = 0; i < s.length; i++)
			s[i] *= demand;
		return s;
	}

	/**
	 * Calculates the total requirements of the respective product amounts to
	 * fulfill the demand of the product system:
	 * <p>
	 * tr = s .* diag(A)
	 * <p>
	 * where s is the scaling vector and A the technology matrix.
	 */
	public double[] getTotalRequirements(IMatrix techMatrix,
										 double[] scalingVector) {
		double[] tr = new double[scalingVector.length];
		for (int i = 0; i < scalingVector.length; i++) {
			tr[i] = scalingVector[i] * techMatrix.get(i, i);
		}
		return tr;
	}

	public static double getLoopFactor(
			IMatrix A, double[] s, TechIndex techIndex) {
		int i = techIndex.getIndex(techIndex.getRefFlow());
		double t = A.get(i, i) * s[i];
		double f = techIndex.getDemand();
		if (Math.abs(t - f) < 1e-12)
			return 1;
		return f / t;
	}

	/**
	 * Calculate the real demand vector for the analysis.
	 */
	public double[] getRealDemands(double[] totalRequirements,
								   double loopFactor) {
		double[] rd = new double[totalRequirements.length];
		if (loopFactor != 1) {
			for (int k = 0; k < totalRequirements.length; k++)
				rd[k] = loopFactor * totalRequirements[k];
		} else {
			int length = totalRequirements.length;
			System.arraycopy(totalRequirements, 0, rd, 0, length);
		}
		return rd;
	}

	private void addDirectImpacts(ContributionResult result) {
		IMatrix factors = data.impactMatrix;
		result.impactFactors = factors;
		result.directImpactResults = solver.multiply(factors,
				result.directFlowResults);
		IMatrix singleFlowImpacts = factors.copy();
		singleFlowImpacts.scaleColumns(result.totalFlowResults);
		result.directFlowImpacts = singleFlowImpacts;
	}

	private void addDirectCosts(ContributionResult result,
								double[] scalingVector) {
		double[] costValues = data.costVector;
		double[] directCosts = new double[costValues.length];
		for (int i = 0; i < scalingVector.length; i++) {
			directCosts[i] = costValues[i] * scalingVector[i];
		}
		result.directCostResults = directCosts;
	}

}
