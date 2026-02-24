package org.openlca.core.math;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.library.reader.LibReaderRegistry;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
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
	private LibReaderRegistry libraries;
	private MatrixSolver solver;

	public SystemCalculator(IDatabase db) {
		this.db = db;
	}

	public SystemCalculator withLibraries(LibReaderRegistry libraries) {
		this.libraries = libraries;
		return this;
	}

	public SystemCalculator withLibraries(LibraryDir libDir) {
		if (libDir != null) {
			this.libraries = LibReaderRegistry.of(db, libDir);
		}
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

	/**
	 * Builds only the tech index for the given setup. The product system structure
	 * does not depend on parameter values, so this can be cached and reused across
	 * calculations that use the same product system but different parameters.
	 */
	public TechIndex buildTechIndex(CalculationSetup setup) {
		return TechIndex.of(db, setup);
	}

	/**
	 * Builds the tech index, sub-results, and matrix data without solving.
	 * Useful for caching: cache the returned data and call {@link #solveWithData}
	 * with the current demand set on the cached {@link MatrixData}.
	 */
	public BuildResult buildMatrixData(CalculationSetup setup) {
		return buildMatrixData(setup, null);
	}

	/**
	 * Builds sub-results and matrix data using the given tech index (e.g. from cache).
	 * When the product system does not change but parameters do, cache the tech index
	 * and call this with it so only matrix build and solve run per request.
	 *
	 * @param setup     calculation setup (parameters, demand, etc.)
	 * @param techIndex optional pre-built tech index; if null, it is built from {@code setup}
	 */
	public BuildResult buildMatrixData(CalculationSetup setup, TechIndex techIndex) {
		return buildMatrixData(setup, techIndex, null, null, null);
	}

	/**
	 * Builds sub-results and matrix data, reusing cached indices when provided.
	 * Cache TechIndex, EnviIndex, ImpactIndex and impact matrix from the first build,
	 * then pass them here so only parameter-dependent tech/envi matrix values are rebuilt.
	 *
	 * @param setup             calculation setup (parameters, demand, etc.)
	 * @param techIndex         optional pre-built tech index
	 * @param cachedEnviIndex   optional pre-built envi index
	 * @param cachedImpactIndex optional pre-built impact index
	 * @param cachedImpactMatrix optional pre-built impact matrix (characterization factors)
	 */
	public BuildResult buildMatrixData(
			CalculationSetup setup,
			TechIndex techIndex,
			EnviIndex cachedEnviIndex,
			ImpactIndex cachedImpactIndex,
			MatrixReader cachedImpactMatrix
	) {
		if (techIndex == null) {
			techIndex = TechIndex.of(db, setup);
		}
		var subs = solveSubSystems(setup, techIndex);
		var builder = MatrixData.of(db, techIndex)
				.withSetup(setup)
				.withSubResults(subs);
		if (cachedEnviIndex != null) {
			builder.withCachedEnviIndex(cachedEnviIndex);
		}
		if (cachedImpactIndex != null) {
			builder.withCachedImpactIndex(cachedImpactIndex);
		}
		if (cachedImpactMatrix != null) {
			builder.withCachedImpactMatrix(cachedImpactMatrix);
		}
		var data = builder.build();
		return new BuildResult(data, subs);
	}

	/**
	 * Solves using pre-built matrix data (e.g. from cache). The demand in
	 * {@code data.demand} is used; set it to the current request's demand
	 * before calling if reusing cached data.
	 */
	public LcaResult solveWithData(MatrixData data, Map<TechFlow, LcaResult> subResults) {
		long totalStart = System.nanoTime();
		int techRows = data.techMatrix != null ? data.techMatrix.rows() : 0;
		int techCols = data.techMatrix != null ? data.techMatrix.columns() : 0;
		int enviRows = data.enviMatrix != null ? data.enviMatrix.rows() : 0;
		int enviCols = data.enviMatrix != null ? data.enviMatrix.columns() : 0;
		int subCount = subResults != null ? subResults.size() : 0;
		log.info("[PROFILE] solveWithData started (techMatrix={}x{}, enviMatrix={}x{}, enviIndex.size={}, subResults.count={})",
				techRows, techCols, enviRows, enviCols,
				data.enviIndex != null ? data.enviIndex.size() : 0, subCount);

		long t0 = System.nanoTime();
		var context = SolverContext.of(db, data)
				.withLibraries(libraries)
				.withSolver(solver);
		long contextMs = (System.nanoTime() - t0) / 1_000_000;
		log.info("[PROFILE] SolverContext.of: {} ms", contextMs);

		t0 = System.nanoTime();
		var provider = ResultProviders.solve(context);
		long solveMs = (System.nanoTime() - t0) / 1_000_000;
		log.info("[PROFILE] ResultProviders.solve: {} ms, provider={}", solveMs, provider);

		t0 = System.nanoTime();
		var result = new LcaResult(provider);
		if (subResults != null) {
			for (var sub : subResults.entrySet()) {
				var techFlow = sub.getKey();
				var subResult = sub.getValue();
				if (techFlow.isProductSystem()) {
					result.addSubResult(techFlow, subResult);
				}
			}
		}
		long resultWrapMs = (System.nanoTime() - t0) / 1_000_000;
		log.info("[PROFILE] LcaResult + addSubResults: {} ms", resultWrapMs);

		long totalMs = (System.nanoTime() - totalStart) / 1_000_000;
		log.info("[PROFILE] solveWithData total: {} ms (Context={}, Solve={}, Wrap={})",
				totalMs, contextMs, solveMs, resultWrapMs);

		return result;
	}

	/** Result of {@link #buildMatrixData}: matrix data and sub-results to cache. */
	public static final class BuildResult {
		public final MatrixData data;
		public final Map<TechFlow, LcaResult> subResults;

		public BuildResult(MatrixData data, Map<TechFlow, LcaResult> subResults) {
			this.data = data;
			this.subResults = subResults != null ? subResults : Collections.emptyMap();
		}
	}

	private LcaResult solve(CalculationSetup setup, int type) {
		long totalStart = System.nanoTime();
		log.info("[PROFILE] product system calculation started for {}", setup.target());

		long t0 = System.nanoTime();
		var techIndex = TechIndex.of(db, setup);
		int techSize = techIndex != null ? techIndex.size() : 0;
		long techIndexMs = (System.nanoTime() - t0) / 1_000_000;
		log.info("[PROFILE] TechIndex.of: {} ms (techIndex.size={})", techIndexMs, techSize);

		t0 = System.nanoTime();
		var subs = solveSubSystems(setup, techIndex);
		long subSystemsMs = (System.nanoTime() - t0) / 1_000_000;
		log.info("[PROFILE] solveSubSystems: {} ms (subs.count={})", subSystemsMs, subs.size());

		t0 = System.nanoTime();
		var data = MatrixData.of(db, techIndex)
				.withSetup(setup)
				.withSubResults(subs)
				.build();
		long matrixDataMs = (System.nanoTime() - t0) / 1_000_000;
		int techRows = data.techMatrix != null ? data.techMatrix.rows() : 0;
		int techCols = data.techMatrix != null ? data.techMatrix.columns() : 0;
		int enviRows = data.enviMatrix != null ? data.enviMatrix.rows() : 0;
		int enviCols = data.enviMatrix != null ? data.enviMatrix.columns() : 0;
		log.info("[PROFILE] MatrixData.build: {} ms (techMatrix={}x{}, enviMatrix={}x{}, enviIndex.size={})",
				matrixDataMs, techRows, techCols, enviRows, enviCols,
				data.enviIndex != null ? data.enviIndex.size() : 0);

		t0 = System.nanoTime();
		var context = SolverContext.of(db, data)
				.withLibraries(libraries)
				.withSolver(solver);
		long contextMs = (System.nanoTime() - t0) / 1_000_000;
		log.info("[PROFILE] SolverContext.of: {} ms", contextMs);

		t0 = System.nanoTime();
		var provider = switch (type) {
			case LAZY -> ResultProviders.solveLazy(context);
			case EAGER -> ResultProviders.solveEager(context);
			default -> ResultProviders.solve(context);
		};
		long solveMs = (System.nanoTime() - t0) / 1_000_000;
		log.info("[PROFILE] ResultProviders.solve ({}): {} ms, provider={}", typeName(type), solveMs, provider);

		t0 = System.nanoTime();
		var result = new LcaResult(provider);
		for (var sub : subs.entrySet()) {
			var techFlow = sub.getKey();
			var subResult = sub.getValue();
			if (techFlow.isProductSystem()) {
				result.addSubResult(techFlow, subResult);
			}
		}
		long resultWrapMs = (System.nanoTime() - t0) / 1_000_000;
		log.info("[PROFILE] LcaResult + addSubResults: {} ms", resultWrapMs);

		long totalMs = (System.nanoTime() - totalStart) / 1_000_000;
		log.info("[PROFILE] product system calculation total: {} ms (TechIndex={}, SubSystems={}, MatrixData={}, Context={}, Solve={}, Wrap={})",
				totalMs, techIndexMs, subSystemsMs, matrixDataMs, contextMs, solveMs, resultWrapMs);

		return result;
	}

	private static String typeName(int type) {
		return switch (type) {
			case 1 -> "LAZY";
			case 2 -> "EAGER";
			default -> "DEFAULT";
		};
	}

	/// Calculates (recursively) the sub-systems of the given setup. It returns an
	/// empty map when there are no subsystems. For the sub-results, the same
	/// calculation type is performed as defined in the original calculation setup.
	private Map<TechFlow, LcaResult> solveSubSystems(
			CalculationSetup setup, TechIndex techIndex
	) {
		if (techIndex == null)
			return Collections.emptyMap();

		long t0 = System.nanoTime();
		var subResults = new HashMap<TechFlow, LcaResult>();
		var subSystems = new HashSet<TechFlow>();
		int resultCount = 0;
		for (var p : techIndex) {
			if (p.isProcess())
				continue;
			if (p.isProductSystem()) {
				subSystems.add(p);
				continue;
			}
			if (p.isResult()) {
				var result = db.get(Result.class, p.providerId());
				if (result != null) {
					subResults.put(p, new LcaResult(ResultModelProvider.of(result)));
					resultCount++;
				}
			}
		}
		long resultsLoadMs = (System.nanoTime() - t0) / 1_000_000;
		if (resultCount > 0 || !subSystems.isEmpty()) {
			log.info("[PROFILE] solveSubSystems: index scan + {} Result(s) loaded in {} ms, {} ProductSystem(s) to solve",
					resultCount, resultsLoadMs, subSystems.size());
		}

		if (subSystems.isEmpty())
			return subResults;

		// calculate the LCI results of the sub-systems
		for (var p : subSystems) {
			var sub = db.get(ProductSystem.class, p.providerId());
			if (sub == null)
				continue;

			long subT0 = System.nanoTime();
			var subSetup = CalculationSetup.of(sub)
					.withParameters(ParameterRedefs.join(setup, sub))
					.withCosts(setup.hasCosts())
					.withRegionalization(setup.hasRegionalization())
					.withAllocation(setup.allocation())
					.withImpactMethod(setup.impactMethod())
					.withNwSet(setup.nwSet());
			var subResult = calculate(subSetup);
			long subMs = (System.nanoTime() - subT0) / 1_000_000;
			log.info("[PROFILE] solveSubSystems: sub-system {} calculated in {} ms", sub.name, subMs);
			subResults.put(p, subResult);
		}
		return subResults;
	}
}
