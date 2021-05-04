package org.openlca.core.math;

import java.util.HashSet;
import java.util.Map;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.InventoryBuilder;
import org.openlca.core.matrix.InventoryConfig;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.openlca.core.results.SimpleResult;
import org.openlca.expressions.FormulaInterpreter;

import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * Provides helper methods for creating matrix-like data structures that can be
 * used in calculations (but also exports, validations, etc.).
 */
public class DataStructures {

	private DataStructures() {
	}

	/**
	 * Creates a product index from the given product system.
	 */
	public static TechIndex createProductIndex(
			ProductSystem system, IDatabase db) {

		// initialize the TechIndex with the reference flow
		Exchange refExchange = system.referenceExchange;
		ProcessProduct refFlow = ProcessProduct.of(
				system.referenceProcess, refExchange.flow);
		TechIndex index = new TechIndex(refFlow);

		// set the final demand value which is negative
		// when we have a waste flow as reference flow
		double demand = ReferenceAmount.get(system);
		FlowType ftype = system.referenceExchange == null
				? null
				: system.referenceExchange.flow == null
						? null
						: system.referenceExchange.flow.flowType;
		if (ftype == FlowType.WASTE_FLOW) {
			demand = -demand;
		}
		index.setDemand(demand);

		// initialize the fast descriptor maps
		ProductSystemDao sysDao = new ProductSystemDao(db);
		TLongObjectHashMap<ProductSystemDescriptor> systems = sysDao
				.descriptorMap();
		TLongObjectHashMap<ProcessDescriptor> processes = new ProcessDao(
				db).descriptorMap();
		TLongObjectHashMap<FlowDescriptor> flows = new FlowDao(
				db).descriptorMap();

		for (ProcessLink link : system.processLinks) {
			CategorizedDescriptor p = processes.get(link.providerId);
			if (p == null) {
				p = systems.get(link.providerId);
				if (p == null)
					continue;
			}
			FlowDescriptor flow = flows.get(link.flowId);
			if (flow == null)
				continue;

			// the tech-index checks for duplicates of products and links
			ProcessProduct provider = ProcessProduct.of(p, flow);
			index.put(provider);
			LongPair exchange = new LongPair(link.processId,
					link.exchangeId);
			index.putLink(exchange, provider);
		}
		return index;
	}

	/**
	 * Create the matrix data for the calculation of the given setup.
	 */
	public static MatrixData matrixData(
			CalculationSetup setup,
			IMatrixSolver solver,
			MatrixCache mcache,
			Map<ProcessProduct, SimpleResult> subResults) {

		IDatabase db = mcache.getDatabase();
		TechIndex techIndex = createProductIndex(setup.productSystem, db);
		techIndex.setDemand(setup.getDemandValue());
		FormulaInterpreter interpreter = interpreter(
				db, setup, techIndex);

		InventoryConfig conf = new InventoryConfig(db, techIndex);
		conf.allocationMethod = setup.allocationMethod;
		conf.interpreter = interpreter;
		conf.subResults = subResults;
		conf.withCosts = setup.withCosts;
		conf.withUncertainties = setup.type == CalculationType.MONTE_CARLO_SIMULATION;
		InventoryBuilder builder = new InventoryBuilder(conf);

		MatrixData data = builder.build();
		if (setup.impactMethod != null) {
			ImpactTable impacts = ImpactTable.build(
					mcache, setup.impactMethod.id, data.enviIndex);
			if(impacts == null) return data;
			data.impactMatrix = impacts.createMatrix(
					solver, interpreter);
			data.impactIndex = impacts.impactIndex;
		}
		return data;
	}

	public static FormulaInterpreter interpreter(IDatabase db,
			CalculationSetup setup, TechIndex techIndex) {
		// collect the process and LCIA method IDs; these
		// are the possible contexts of local parameters
		HashSet<Long> contexts = new HashSet<>();
		if (setup != null && setup.impactMethod != null) {
			contexts.add(setup.impactMethod.id);
		}
		if (techIndex != null) {
			contexts.addAll(techIndex.getProcessIds());
		}
		return ParameterTable.interpreter(
				db, contexts, setup.parameterRedefs);
	}
}
