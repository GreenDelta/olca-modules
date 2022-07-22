package org.openlca.core.results.providers;

import org.openlca.core.results.providers.libblocks.LibraryInversionSolver;

public final class ResultProviders {

	private ResultProviders() {
	}

	/**
	 * Solves the given context using the best matching solver.
	 */
	public static ResultProvider solve(SolverContext context) {

		// solve library results on-demand
		if (context.hasLibraryLinks())
			return LazyLibrarySolver.solve(context);

		var data = context.data();
		var solver = context.solver();

		// small systems can be quickly calculated with full-matrix inversion
		var smallLimit = solver.isNative()
			? 5000
			: 500;
		if (data.techMatrix.rows() < smallLimit)
			return InversionResult.of(context).calculate().provider();

		// solve via factorization by default
		return FactorizationSolver.solve(context);
	}

	/**
	 * Solves the context eagerly. This means it calculates all possible results
	 * for a system. This typically includes a full matrix inversion.
	 */
	public static ResultProvider solveEager(SolverContext context) {
		return context.hasLibraryLinks()
			? LibraryInversionSolver.solve(context)
			: InversionResult.of(context).calculate().provider();
	}

	/**
	 * Returns a result provider that calculates results on demand.
	 */
	public static ResultProvider solveLazy(SolverContext context) {
		return context.hasLibraryLinks()
			? LazyLibrarySolver.solve(context)
			: FactorizationSolver.solve(context);
	}
}
