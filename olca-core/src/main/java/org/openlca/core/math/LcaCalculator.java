package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;
import org.openlca.core.results.providers.EagerResultProvider;
import org.openlca.core.results.providers.LazyResultProvider;
import org.openlca.core.results.providers.LibraryResultProvider;
import org.openlca.core.results.providers.ResultProvider;

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
		return new SimpleResult(solution(true));
	}

	public ContributionResult calculateContributions() {
		return new ContributionResult(solution(true));
	}

	public FullResult calculateFull() {
		return new FullResult(solution(false));
	}

	/**
	 * TODO replace with $diag(A) \odot diag(A^{-1})$
	 *
	 * @deprecated
	 */
	@Deprecated
	public static double getLoopFactor(
		MatrixReader A, double[] s, TechIndex techIndex) {
		int i = techIndex.of(techIndex.getRefFlow());
		double t = A.get(i, i) * s[i];
		double f = techIndex.getDemand();
		if (Math.abs(t - f) < 1e-12)
			return 1;
		return f / t;
	}

}
