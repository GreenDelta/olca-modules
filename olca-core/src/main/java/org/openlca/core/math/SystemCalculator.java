package org.openlca.core.math;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Result;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.providers.ResultModelProvider;
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

	private final int DEFAULT = 0;
	private final int LAZY = 1;
	private final int EAGER = 2;

	private final IDatabase db;
	private LibraryDir libraryDir;
	private MatrixSolver solver;

	public SystemCalculator(IDatabase db) {
		this.db = db;
	}

	public SystemCalculator withLibraryDir(LibraryDir libraryDir) {
		this.libraryDir = libraryDir;
		return this;
	}

	public SystemCalculator withSolver(MatrixSolver solver) {
		this.solver = solver;
		return this;
	}

	public LcaResult calculate(CalculationSetup setup) {
		return solve(setup, DEFAULT);
	}

	public LcaResult calculateLazy(CalculationSetup setup) {
		return solve(setup, LAZY);
	}

	public LcaResult calculateEager(CalculationSetup setup) {
		return solve(setup, EAGER);
	}

	private LcaResult solve(CalculationSetup setup, int type) {
		log.info("calculate result for {}", setup.target());
		var techIndex = TechIndex.of(db, setup);
		var subs = solveSubSystems(setup, techIndex);
		log.trace("solved {} sub-systems", subs.size());
		var data = MatrixData.of(db, techIndex)
				.withSetup(setup)
				.withSubResults(subs)
				.build();
		var context = SolverContext.of(db, data)
				.libraryDir(libraryDir)
				.solver(solver);

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

	/**
	 * Calculates (recursively) the sub-systems of the product system of the
	 * given setup. It returns an empty map when there are no subsystems. For
	 * the sub-results, the same calculation type is performed as defined in
	 * the original calculation setup.
	 */
	private Map<TechFlow, LcaResult> solveSubSystems(
			CalculationSetup setup, TechIndex techIndex) {
		if (setup == null || !setup.hasProductSystem())
			return Collections.emptyMap();

		var subResults = new HashMap<TechFlow, LcaResult>();

		var subSystems = new HashSet<TechFlow>();
		for (var link : setup.productSystem().processLinks) {
			if (link.hasProcessProvider())
				continue;
			var provider = techIndex.getProvider(link.providerId, link.flowId);
			if (provider == null || provider.isProcess())
				continue;
			if (provider.isProductSystem()) {
				subSystems.add(provider);
				continue;
			}

			// add a result
			if (provider.isResult()) {
				var result = db.get(Result.class, provider.providerId());
				if (result != null) {
					subResults.put(provider, new LcaResult(ResultModelProvider.of(result)));
				}
			}
		}
		if (subSystems.isEmpty())
			return subResults;

		// calculate the LCI results of the sub-systems

		for (var pp : subSystems) {
			var subSystem = db.get(ProductSystem.class, pp.providerId());
			if (subSystem == null)
				continue;

			var subSetup = CalculationSetup.of(subSystem)
					.withParameters(ParameterRedefs.join(setup, subSystem))
					.withCosts(setup.hasCosts())
					.withRegionalization(setup.hasRegionalization())
					.withAllocation(setup.allocation())
					.withImpactMethod(setup.impactMethod())
					.withNwSet(setup.nwSet());
			var subResult = calculate(subSetup);
			subResults.put(pp, subResult);
		}
		return subResults;
	}
}
