package org.openlca.core.results.providers;

import java.util.ArrayList;
import java.util.HashSet;

import org.openlca.core.library.LibMatrix;
import org.openlca.core.library.reader.LibReader;

public final class ResultProviders {

	private ResultProviders() {
	}

	/**
	 * Solves the given context using the best matching solver.
	 */
	public static ResultProvider solve(SolverContext ctx) {
		if (ctx.hasLibraryLinks())
			return selectLibrarySolver(ctx);

		var data = ctx.data();
		var solver = ctx.solver();

		// small systems can be quickly calculated with full-matrix inversion
		var smallLimit = solver.isNative()
			? 5000
			: 500;
		if (data.techMatrix.rows() < smallLimit)
			return InversionResult.of(ctx).calculate().provider();

		// solve via factorization by default
		return FactorizationSolver.solve(ctx);
	}

	/**
	 * Solves the context eagerly. This means it calculates all possible results
	 * for a system. This typically includes a full matrix inversion.
	 */
	public static ResultProvider solveEager(SolverContext ctx) {
		return ctx.hasLibraryLinks()
			? selectLibrarySolver(ctx)
			: InversionResult.of(ctx).calculate().provider();
	}

	/**
	 * Returns a result provider that calculates results on demand.
	 */
	public static ResultProvider solveLazy(SolverContext ctx) {
		return ctx.hasLibraryLinks()
			? selectLibrarySolver(ctx)
			: FactorizationSolver.solve(ctx);
	}

	/// Try to select the best library solver for the given context. If there
	/// are dependencies between process libraries or the process libraries are
	/// not pre-calculated (means that they should be sparse), we load all data
	/// into memory first and then use a standard solver. Otherwise, we use the
	/// `LazyLibrarySolver` by default.
	private static ResultProvider selectLibrarySolver(SolverContext ctx) {
		var techIdx = ctx.data().techIndex;
		var libReaders = new ArrayList<LibReader>();
		var libNames = new HashSet<String>();
		for (var techFlow : techIdx) {
			if (!techFlow.isFromLibrary() || libNames.contains(techFlow.library()))
				continue;
			libNames.add(techFlow.library());
			var lib = ctx.libraries().get(techFlow.library());
			if (lib != null) {
				libReaders.add(lib);
			}
		}

		// if there are no libraries for technosphere data, we load the impact
		// model into memory
		if (libReaders.isEmpty())
			return InMemLibrarySolver.solve(ctx);

		boolean hasPrecalculated = false;

		for (var r : libReaders) {
			if (r.library().hasMatrix(LibMatrix.INV)) {
				hasPrecalculated = true;
			}
			var deps = r.library().getDirectDependencies();
			for (var dep : deps) {
				// there is a process library dependency, we combine
				// everything in memory
				if (dep.hasMatrix(LibMatrix.A))
					return InMemLibrarySolver.solve(ctx);
			}
		}

		return hasPrecalculated
				? LazyLibrarySolver.solve(ctx)
				: InMemLibrarySolver.solve(ctx);
	}
}
