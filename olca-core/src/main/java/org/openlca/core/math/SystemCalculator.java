package org.openlca.core.math;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.library.reader.LibReaderRegistry;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.cache.MatrixBuildContext;
import org.openlca.core.matrix.ResultVectorBuilder;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.providers.ResultProviders;
import org.openlca.core.results.providers.SolverContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates the results of a calculation setup. The product systems of the
 * setups may contain sub-systems which are calculated recursively. The
 * calculator does not check if there are obvious errors like sub-system cycles
 * etc.
 */
public class SystemCalculator {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final int LAZY = 1;
	private final int EAGER = 2;

	private final MatrixBuildContext ctx;
	private LibReaderRegistry libraries;
	private MatrixSolver solver;

	public SystemCalculator(IDatabase db) {
		this.ctx = MatrixBuildContext.of(db);
	}

	public SystemCalculator withLibraries(LibReaderRegistry libraries) {
		this.libraries = libraries;
		return this;
	}

	public SystemCalculator withLibraries(LibraryDir libDir) {
		if (libDir != null) {
			this.libraries = LibReaderRegistry.of(ctx.db(), libDir);
		}
		return this;
	}

	public SystemCalculator withSolver(MatrixSolver solver) {
		this.solver = solver;
		return this;
	}

	public LcaResult calculate(CalculationSetup setup) {
		return solve(setup, 0);
	}

	public LcaResult calculateLazy(CalculationSetup setup) {
		return solve(setup, LAZY);
	}

	public LcaResult calculateEager(CalculationSetup setup) {
		return solve(setup, EAGER);
	}

	private LcaResult solve(CalculationSetup setup, int type) {
		log.info("calculate result for {}", setup.target());
		var techIndex = TechIndex.of(ctx.db(), setup);
		var subs = solveSubSystems(setup, techIndex);
		log.trace("solved {} sub-systems", subs.size());
		var data = MatrixData.of(ctx, techIndex)
			.withSetup(setup)
			.withSubResults(subs)
			.build();
		var context = SolverContext.of(ctx.db(), data)
			.withLibraries(libraries)
			.withSolver(solver);

		var provider = switch (type) {
			case LAZY -> ResultProviders.solveLazy(context);
			case EAGER -> ResultProviders.solveEager(context);
			default -> ResultProviders.solve(context);
		};
		log.info("selected provider {}", provider);

		var result = new LcaResult(provider);

		for (var sub : subs.entrySet()) {
			var techFlow = sub.getKey();
			var subResult = sub.getValue();
			// for sub-systems add the sub-result
			if (techFlow.isProductSystem()) {
				result.addSubResult(techFlow, subResult);
			}
		}

		return result;
	}

	/// Calculates (recursively) the sub-systems of the given setup. It returns an
	/// empty map when there are no subsystems. For the sub-results, the same
	/// calculation type is performed as defined in the original calculation setup.
	private Map<TechFlow, LcaResult> solveSubSystems(
		CalculationSetup setup, TechIndex techIndex
	) {
		if (techIndex == null)
			return Collections.emptyMap();

		// add vectors of linked results
		var subResults = new HashMap<TechFlow, LcaResult>();
		ResultVectorBuilder.of(setup, techIndex, ctx)
			.ifPresent(builder -> builder.each(
				(techFlow, vec) -> subResults.put(techFlow, new LcaResult(vec))));

		// calculate LCI results of sub-systems
		for (var p : techIndex) {
			if (!p.isProductSystem())
				continue;
			var sub = ctx.db().get(ProductSystem.class, p.providerId());
			if (sub == null)
				continue;
			var subSetup = CalculationSetup.of(sub)
				.withParameters(ParameterRedefs.join(setup, sub))
				.withCosts(setup.hasCosts())
				.withRegionalization(setup.hasRegionalization())
				.withAllocation(setup.allocation())
				.withImpactMethod(setup.impactMethod())
				.withNwSet(setup.nwSet());
			var subResult = calculate(subSetup);
			subResults.put(p, subResult);
		}

		return subResults;
	}
}
