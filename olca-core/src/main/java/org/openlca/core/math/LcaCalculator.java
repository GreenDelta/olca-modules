package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;
import org.openlca.core.results.solutions.EagerResultProvider;
import org.openlca.core.results.solutions.LazyResultProvider;
import org.openlca.core.results.solutions.LibraryResultProvider;
import org.openlca.core.results.solutions.ResultProvider;
import org.openlca.julia.Julia;
import org.openlca.julia.JuliaSolver;

/**
 * This calculator does the low level matrix based LCA-calculation. Typically,
 * you do not want to use this directly but a more high level calculator where
 * you can directly throw in a calculation setup or project.
 */
@Deprecated
public class LcaCalculator {

	private final MatrixData data;
	private final IDatabase db;

	public LcaCalculator(IDatabase db, MatrixData data) {
		this.data = data;
		this.data.compress();
		this.db = db;
	}

	private ResultProvider solution(boolean forceLazy) {
		if (data.hasLibraryLinks())
			return LibraryResultProvider.of(db, data);
		if (forceLazy)
			return LazyResultProvider.create(data);
		if (!data.isSparse())
			return EagerResultProvider.create(data);
		var solver = MatrixSolver.Instance.getNew();
		return solver.hasSparseSupport()
			? LazyResultProvider.create(data)
			: EagerResultProvider.create(data);
	}

	public SimpleResult calculateSimple() {
		var solution = solution(true);
		var result = new SimpleResult();
		fillSimple(result, solution);
		return result;
	}

	private void fillSimple(SimpleResult r, ResultProvider s) {
		r.techIndex = s.techIndex();
		r.flowIndex = s.flowIndex();
		r.impactIndex = s.impactIndex();
		r.scalingVector = s.scalingVector();
		r.totalRequirements = s.totalRequirements();

		if (r.flowIndex != null && !r.flowIndex.isEmpty()) {
			r.totalFlowResults = s.totalFlows();
			if (r.impactIndex != null && !r.impactIndex.isEmpty()) {
				r.totalImpactResults = s.totalImpacts();
			}
		}
		if (s.hasCosts()) {
			r.totalCosts = s.totalCosts();
		}
	}

	public ContributionResult calculateContributions() {
		var solution = solution(true);
		var result = new ContributionResult(solution);
		fillSimple(result, solution);
		return result;
	}

	public FullResult calculateFull() {
		var solution = solution(false);
		var result = new FullResult(solution);
		fillSimple(result, solution);
		return result;
	}

	/**
	 * TODO replace with $diag(A) \odot diag(A^{-1})$
	 *
	 * @deprecated
	 */
	@Deprecated
	public static double getLoopFactor(
		MatrixReader A, double[] s, TechIndex techIndex) {
		int i = techIndex.getIndex(techIndex.getRefFlow());
		double t = A.get(i, i) * s[i];
		double f = techIndex.getDemand();
		if (Math.abs(t - f) < 1e-12)
			return 1;
		return f / t;
	}

}
