package org.openlca.core.math;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.SimpleResult;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.util.TopoSort;

/**
 * When running the Monte Carlo simulation on a product system $s_r$ that has a
 * sub-system (which again can have sub-systems etc.) we need to first run the
 * number generation and calculation for that sub-system and integrate these
 * results into the matrices of $s_r$ in each iteration step of the simulation.
 * In general, we have to do this for each relation $s_i \prec s_j$, where $s_i$
 * is a sub-system of $s_j$, of all product systems $S$ of the recursively
 * expanded sub-system dependencies.
 *
 * $S$ is a [strict partial ordered
 * set](https://en.wikipedia.org/wiki/Partially_ordered_set#Strict_and_non-strict_partial_orders)
 * as we do not allow cycles in the sub-system dependencies. Thus, we can define
 * a linear order of all systems via [topological
 * sorting](https://en.wikipedia.org/wiki/Topological_sorting) which maps each
 * product system $s_i$ to a position $pos_i$ with $pos_i < pos_j$ when $s_i
 * \prec s_j$.
 *
 * In the simulation, we then run the number generation and calculation for each
 * sub-system starting from the lowest position in $pos = [1 \dots n]$ where the
 * top-most product system $s_r$ has the position $pos_r = n$. Thus, in a
 * simulation step a product system $s_j$ can access and integrate the result of
 * a sub-system $s_i$ when $s_i \prec s_j$. With this, the number generation and
 * calculation have to be done only once for each simulation step for each
 * product system $s_i \in S$.
 */
public class SimulationGraph {

	private final IMatrixSolver solver;

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

	private SimulationGraph(IMatrixSolver solver) {
		this.solver = solver;
	}

	public static SimulationGraph build(
			MatrixCache mcache,
			CalculationSetup setup,
			IMatrixSolver solver) {
		SimulationGraph g = new SimulationGraph(solver);
		g.init(mcache, setup);
		return g;
	}

	public SimpleResult nextRun() {
		for (Node sub : subNodes) {
			generateData(sub);
			LcaCalculator calc = new LcaCalculator(solver, sub.data);
			sub.lastResult = calc.calculateSimple();
		}
		generateData(root);
		LcaCalculator calc = new LcaCalculator(solver, root.data);
		return calc.calculateSimple();
	}

	private void generateData(Node node) {
		FormulaInterpreter fi = node.parameters.simulate();
		node.data.simulate(fi);

		if (node.subSystems != null) {
			for (ProcessProduct subLink : node.subSystems) {
				// add the LCI result of the sub-system
				Node sub = nodeIndex.get(subLink.id());
				if (sub == null)
					continue;
				if (sub.lastResult == null
						|| sub.lastResult.totalFlowResults == null)
					continue; // should not happen
				int col = node.data.techIndex.getIndex(subLink);
				if (col < 0)
					continue;
				sub.lastResult.flowIndex.each((subIdx, flow) -> {
					double val = sub.lastResult.totalFlowResults[subIdx];
					int row = node.data.enviIndex.of(flow);
					if (row >= 0) {
						node.data.enviMatrix.set(row, col, val);
					}
				});
			}
		}

		if (node.impactTable != null) {
			node.impactTable.simulate(node.data.impactMatrix, fi);
		}
	}

	private void init(MatrixCache mcache, CalculationSetup setup) {
		try {
			IDatabase db = mcache.getDatabase();
			long rootID = setup.productSystem.id;

			// TODO: check whether the root system has sub-system links,
			// and and only in this case we need to collect and order
			// the sub-system relations

			// systems contains the IDs of all product systems;
			// with this we can quickly check if an ID is an
			// ID of a product system
			HashSet<Long> systems = new HashSet<>();
			String sql = "select id from tbl_product_systems";
			NativeSql.on(db).query(sql, r -> {
				systems.add(r.getLong(1));
				return true;
			});

			// allRels contains the sub-system relations of each product system
			// in the database as: hostSystemID -> (subSystemID, hostSystemID)*
			Map<Long, List<LongPair>> allRels = new HashMap<>();
			sql = "select f_product_system, f_provider from tbl_process_links";
			NativeSql.on(db).query(sql, r -> {
				long provider = r.getLong(2);
				if (!systems.contains(provider))
					return true;
				long system = r.getLong(1);
				List<LongPair> rels = allRels.get(system);
				if (rels == null) {
					rels = new ArrayList<>();
					allRels.put(system, rels);
				}
				rels.add(LongPair.of(provider, system));
				return true;
			});

			// now collect the sub-system relations that we need to consider
			HashSet<LongPair> sysRels = new HashSet<>();
			Queue<Long> queue = new ArrayDeque<>();
			queue.add(rootID);
			HashSet<Long> handled = new HashSet<>();
			handled.add(rootID);
			while (!queue.isEmpty()) {
				long nextID = queue.poll();
				List<LongPair> rels = allRels.get(nextID);
				if (rels == null)
					continue;
				sysRels.addAll(rels);
				for (LongPair rel : rels) {
					long subSystem = rel.first;
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
			Map<ProcessProduct, SimpleResult> subResults = new HashMap<>();
			for (long system : order) {

				CalculationSetup _setup = null;
				if (system == rootID) {
					_setup = setup;
				} else {
					// create node for LCI and LCC data simulation
					// do *not* copy the LCIA method here
					ProductSystemDao dao = new ProductSystemDao(db);
					ProductSystem sub = dao.getForId(system);
					_setup = new CalculationSetup(
							CalculationType.MONTE_CARLO_SIMULATION, sub);
					_setup.parameterRedefs.addAll(sub.parameterRedefs);
					_setup.withCosts = setup.withCosts;
					_setup.allocationMethod = setup.allocationMethod;
				}

				Node node = new Node(_setup, mcache, subResults);
				nodeIndex.put(system, node);
				if (system == rootID) {
					root = node;
				} else {
					subNodes.add(node);

					// for the sub-nodes we need to initialize an empty
					// result so that the respective host-systems will
					// be initialized with the correct matrix shapes (
					// e.g. flows that only occure in a sub-system
					// need a row in the respective host-systems)
					SimpleResult r = new SimpleResult();
					r.techIndex = node.data.techIndex;
					r.flowIndex = node.data.enviIndex;
					r.totalFlowResults = new double[r.flowIndex.size()];
					node.lastResult = r;
					subResults.put(node.product, r);
				}
			}

			// finally, we add the sub-system links to the nodes so
			// the we do not need to collect them in the simulation
			for (Node node : nodeIndex.values()) {
				List<LongPair> subRels = allRels.get(node.systemID);
				if (subRels == null || subRels.isEmpty())
					continue;
				node.subSystems = new HashSet<>();
				for (LongPair rel : subRels) {
					Node subNode = nodeIndex.get(rel.first);
					if (subNode == null)
						continue;
					node.subSystems.add(subNode.product);
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(
					"building the simulation graph failed", e);
		}

	}

	/**
	 * A node contains the data for the simulation of a single product (sub-)
	 * system.
	 */
	private class Node {
		final long systemID;
		final ProcessProduct product;
		final MatrixData data;
		final ParameterTable parameters;

		// TODO: in later versions this should go into the MatrixData
		final ImpactTable impactTable;

		Set<ProcessProduct> subSystems;
		SimpleResult lastResult;

		Node(CalculationSetup setup, MatrixCache mcache,
				Map<ProcessProduct, SimpleResult> subResults) {

			systemID = setup.productSystem.id;
			product = ProcessProduct.of(setup.productSystem);
			data = DataStructures.matrixData(
					setup, solver, mcache, subResults);

			// parameters
			HashSet<Long> paramContexts = new HashSet<>();
			data.techIndex.each((i, p) -> {
				if (p.process != null
						&& p.process.type == ModelType.PROCESS) {
					paramContexts.add(p.id());
				}
			});
			if (setup.impactMethod != null) {
				paramContexts.add(setup.impactMethod.id);
			}
			parameters = ParameterTable.forSimulation(
					mcache.getDatabase(), paramContexts, setup.parameterRedefs);

			// LCIA factors
			// TODO: see above
			impactTable = setup.impactMethod != null
					? ImpactTable.build(
							mcache, setup.impactMethod.id, data.enviIndex)
					: null;
		}
	}

}
