package org.openlca.core.math;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.LongPair;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.SimulationResult;
import org.openlca.core.results.providers.ResultProvider;
import org.openlca.core.results.providers.ResultProviders;
import org.openlca.core.results.providers.SimpleResultProvider;
import org.openlca.core.results.providers.SolverContext;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.util.TopoSort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A `Simulator` runs Monte-Carlo simulations with a given calculation setup.
 * <p>
 * When running the Monte Carlo simulation on a product system $s_r$ that has a
 * sub-system (which again can have sub-systems etc.) we need to first run the
 * number generation and calculation for that sub-system and integrate these
 * results into the matrices of $s_r$ in each iteration step of the simulation.
 * In general, we have to do this for each relation $s_i \prec s_j$, where $s_i$
 * is a sub-system of $s_j$, of all product systems $S$ of the recursively
 * expanded sub-system dependencies.
 * <p>
 * $S$ is a [strict partial ordered
 * set](https://en.wikipedia.org/wiki/Partially_ordered_set#Strict_and_non-strict_partial_orders)
 * as we do not allow cycles in the sub-system dependencies. Thus, we can define
 * a linear order of all systems via [topological
 * sorting](https://en.wikipedia.org/wiki/Topological_sorting) which maps each
 * product system $s_i$ to a position $pos_i$ with $pos_i < pos_j$ when $s_i
 * \prec s_j$.
 * <p>
 * In the simulation, we then run the number generation and calculation for each
 * sub-system starting from the lowest position in $pos = [1 \dots n]$ where the
 * top-most product system $s_r$ has the position $pos_r = n$. Thus, in a
 * simulation step a product system $s_j$ can access and integrate the result of
 * a sub-system $s_i$ when $s_i \prec s_j$. With this, the number generation and
 * calculation have to be done only once for each simulation step for each
 * product system $s_i \in S$.
 */
public class Simulator {

	/**
	 * A set of products for which upstream and direct contributions should be
	 * tracked during the simulation. These products must be part of the
	 * TechIndex.
	 */
	public final Set<TechFlow> pinnedProducts = new HashSet<>();

	private final IDatabase db;

	/**
	 * The node of the host-system. This is the node that provides the final
	 * data of the Monte-Carlo simulation.
	 */
	private Node root;

	/**
	 * The topological ordered sub-systems (this is empty when the host-system
	 * does not contain sub-systems). The matrix data of the sub-systems do not
	 * contain LCIA data as we only need the LCI (and LCC) results of them. The
	 * same calculation properties of the host-system (allocation method etc.)
	 * are shared with these sub-systems. In a simulation run we can just
	 * calculate the results of the sub-system from $0...n$. When a system $j$
	 * depends on a sub-system $i$ the topological order assures that it was
	 * already calculated before.
	 */
	private final List<Node> subNodes = new ArrayList<>();

	/**
	 * Maps the ID of a product system to the respective node in the simulation
	 * graph. Contains all nodes, also the root system.
	 */
	private final Map<Long, Node> nodeIndex = new HashMap<>();

	private SimulationResult result;
	private LibraryDir libraryDir;
	private MatrixSolver solver;

	private Simulator(IDatabase db) {
		this.db = db;
	}

	public static Simulator create(CalculationSetup setup, IDatabase db) {
		var g = new Simulator(db);
		g.init(db, setup);
		return g;
	}

	public Simulator withSolver(MatrixSolver solver) {
		this.solver = solver;
		return this;
	}

	public Simulator withLibraryDir(LibraryDir libraryDir) {
		this.libraryDir = libraryDir;
		return this;
	}

	/**
	 * Get the result of the simulation.
	 */
	public SimulationResult getResult() {
		if (result != null)
			return result;
		result = new SimulationResult(root.data);
		return result;
	}

	public TechIndex getTechIndex() {
		return root.data.techIndex;
	}

	public EnviIndex getEnviIndex() {
		return root.data.enviIndex;
	}

	public ImpactIndex getImpactIndex() {
		return root.data.impactIndex;
	}

	/**
	 * Generates random numbers and calculates the product system. Returns the
	 * simulation result if the calculation in this run finished without errors,
	 * otherwise `null` is returned (e.g. when the resulting matrix was
	 * singular). The returned result is appended to the result of the simulator
	 * (which you get via `getResult()`, so it does not need to be cached.
	 */
	public LcaResult nextRun() {
		try {

			// generate the numbers and calculate the overall result
			for (var sub : subNodes) {
				generateData(sub);
				sub.lastResult = solve(sub.data);
			}
			generateData(root);
			var next = solve(root.data);
			var provider = next.provider();
			var result = getResult();
			result.append(next);

			// calculate results of possible pinned products
			for (var product : pinnedProducts) {
				int idx = next.techIndex().of(product);
				if (idx < 0)
					continue;
				var pin = result.pin(product);
				if (provider.hasFlows()) {
					pin.withDirectFlows(next.provider().directFlowsOf(idx));
					pin.withUpstreamFlows(provider.totalFlowsOf(idx));
				}
				if (provider.hasImpacts()) {
					pin.withDirectImpacts(provider.directImpactsOf(idx));
					pin.withUpstreamImpacts(provider.totalImpactsOf(idx));
				}
				pin.add();
			}
			return next;
		} catch (Throwable e) {
			Logger log = LoggerFactory.getLogger(this.getClass());
			log.trace("simulation run failed", e);
			return null;
		}
	}

	private LcaResult solve(MatrixData data) {
		var context = SolverContext.of(db, data)
				.solver(solver)
				.libraryDir(libraryDir);
		var provider = ResultProviders.solveLazy(context);
		return new LcaResult(provider);
	}

	private void generateData(Node node) {
		FormulaInterpreter fi = node.parameters.simulate();
		node.data.simulate(fi);

		if (node.subSystems != null) {
			for (TechFlow subLink : node.subSystems) {
				// add the LCI result of the sub-system
				Node sub = nodeIndex.get(subLink.providerId());
				if (sub == null)
					continue;
				if (sub.lastResult == null || !sub.lastResult.hasEnviFlows())
					continue; // should not happen
				int col = node.data.techIndex.of(subLink);
				if (col < 0)
					continue;
				sub.lastResult.enviIndex().each((i, f) -> {
					double val = sub.lastResult.provider().totalFlows()[i];
					int row = node.data.enviIndex.of(f);
					if (row >= 0) {
						var fm = node.data.enviMatrix.asMutable();
						fm.set(row, col, val);
						node.data.enviMatrix = fm;
					}
				});
			}
		}
	}

	private void init(IDatabase db, CalculationSetup setup) {

		// if the calculation target is a process, the simulation graph
		// has a single node
		if (!setup.hasProductSystem()) {
			root = new Node(setup, db, Collections.emptyMap());
			nodeIndex.put(root.providerId, root);
			return;
		}

		// check whether the root system has sub-system links;
		// only when this is true we need to collect and order
		// the sub-system relations
		boolean hasSubSystems = false;
		for (var link : setup.productSystem().processLinks) {
			if (link.hasSubSystemProvider()) {
				hasSubSystems = true;
				break;
			}
		}
		if (!hasSubSystems) {
			root = new Node(setup, db, Collections.emptyMap());
			nodeIndex.put(root.providerId, root);
			return;
		}

		// systems contains the IDs of all product systems;
		// with this we can quickly check if an ID is an
		// ID of a product system
		var systems = new HashSet<Long>();
		var sql = "select id from tbl_product_systems";
		NativeSql.on(db).query(sql, r -> {
			systems.add(r.getLong(1));
			return true;
		});

		// allRels contains the sub-system relations of each product system
		// in the database as: hostSystemID -> (subSystemID, hostSystemID)*
		var allRels = new HashMap<Long, List<LongPair>>();
		sql = "select f_product_system, f_provider from tbl_process_links";
		NativeSql.on(db).query(sql, r -> {
			long provider = r.getLong(2);
			if (!systems.contains(provider))
				return true;
			long system = r.getLong(1);
			var rels = allRels.computeIfAbsent(
					system, k -> new ArrayList<>());
			rels.add(LongPair.of(provider, system));
			return true;
		});

		// now collect the sub-system relations that we need to consider
		long rootId = setup.target().id;
		var sysRels = new HashSet<LongPair>();
		var queue = new ArrayDeque<Long>();
		queue.add(rootId);
		var handled = new HashSet<Long>();
		handled.add(rootId);
		while (!queue.isEmpty()) {
			long nextId = queue.poll();
			var rels = allRels.get(nextId);
			if (rels == null)
				continue;
			sysRels.addAll(rels);
			for (var rel : rels) {
				long subSystem = rel.first();
				if (handled.contains(subSystem))
					continue;
				queue.add(subSystem);
				handled.add(subSystem);
			}
		}

		// now we can initialize the nodes in topological order
		List<Long> order = TopoSort.of(sysRels);
		if (order == null)
			throw new RuntimeException(
					"there are sub-system cycles in the product system");

		// now, we initialize the nodes in topological order
		Map<TechFlow, LcaResult> subResults = new HashMap<>();
		for (long systemId : order) {

			CalculationSetup _setup;
			if (systemId == rootId) {
				_setup = setup;
			} else {
				// create node for LCI and LCC data simulation
				// do *not* set the LCIA method here
				var subSystem = db.get(ProductSystem.class, systemId);
				_setup = CalculationSetup.of(subSystem)
						.withSimulationRuns(setup.simulationRuns().orElse(1))
						.withParameters(ParameterRedefs.join(setup, subSystem))
						.withCosts(setup.hasCosts())
						.withAllocation(setup.allocation());
			}

			Node node = new Node(_setup, db, subResults);
			nodeIndex.put(systemId, node);
			if (systemId == rootId) {
				root = node;
			} else {
				subNodes.add(node);

				// for the sub-nodes we need to initialize an empty
				// result so that the respective host-systems will
				// be initialized with the correct matrix shapes (
				// e.g. flows that only occur in a sub-system
				// need a row in the respective host-systems)
				var enviIdx = node.data.enviIndex;
				var totalFlows = enviIdx != null
						? new double[enviIdx.size()]
						: ResultProvider.EMPTY_VECTOR;
				var r = SimpleResultProvider.of(node.data.demand, node.data.techIndex)
						.withFlowIndex(enviIdx)
						.withTotalFlows(totalFlows)
						.toResult();
				node.lastResult = r;
				subResults.put(node.provider, r);
			}
		}

		// finally, we add the sub-system links to the nodes so
		// that we do not need to collect them in the simulation
		for (Node node : nodeIndex.values()) {
			List<LongPair> subRels = allRels.get(node.providerId);
			if (subRels == null || subRels.isEmpty())
				continue;
			node.subSystems = new HashSet<>();
			for (LongPair rel : subRels) {
				var subNode = nodeIndex.get(rel.first());
				if (subNode == null)
					continue;
				node.subSystems.add(subNode.provider);
			}
		}
	}

	/**
	 * A node contains the data for the simulation of a single product (sub-)
	 * system.
	 */
	private static class Node {
		final long providerId;
		final TechFlow provider;
		final MatrixData data;
		final ParameterTable parameters;

		Set<TechFlow> subSystems;
		LcaResult lastResult;

		Node(CalculationSetup setup, IDatabase db,
				Map<TechFlow, LcaResult> subResults) {

			data = MatrixData.of(db, TechIndex.of(db, setup))
					.withSetup(setup)
					.withUncertainties(true)
					.withSubResults(subResults)
					.build();

			if (setup.hasProductSystem()) {
				providerId = setup.productSystem().id;
				provider = TechFlow.of(setup.productSystem());
			} else {
				providerId = setup.process().id;
				provider = data.demand.techFlow();
			}

			// parameters
			var paramContexts = new HashSet<Long>();
			data.techIndex.each((i, p) -> {
				if (p.provider() != null
						&& p.provider().type == ModelType.PROCESS) {
					paramContexts.add(p.providerId());
				}
			});
			var impactMethod = setup.impactMethod();
			if (impactMethod != null) {
				new ImpactMethodDao(db)
						.getCategoryDescriptors(impactMethod.id)
						.forEach(d -> paramContexts.add(d.id));
			}
			parameters = ParameterTable.forSimulation(
					db, paramContexts, setup.parameters());
		}
	}

}
