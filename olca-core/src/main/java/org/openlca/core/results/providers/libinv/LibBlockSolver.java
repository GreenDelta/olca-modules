package org.openlca.core.results.providers.libinv;

import org.openlca.core.results.providers.LibCache;
import org.openlca.core.results.providers.ResultProvider;
import org.openlca.core.results.providers.SolverContext;

public class LibBlockSolver {

	private final SolverContext context;
	private final LibCache libs;

	public static ResultProvider solve(SolverContext context) {
		return new LibBlockSolver(context).solve();
	}

	private LibBlockSolver(SolverContext context) {
		this.context = context;
		this.libs = LibCache.of(context);
	}

	private ResultProvider solve() {

		var f = context.data();
		var demand = f.demand;

		if (demand.techFlow().isFromLibrary()) {

		}

		return null;
	}

}
