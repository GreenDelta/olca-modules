package org.openlca.core.math;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.matrix.ImpactIndex;
import org.openlca.core.matrix.ImpactBuilder;
import org.openlca.core.matrix.InventoryBuilder;
import org.openlca.core.matrix.InventoryConfig;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
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
	 * Create the matrix data for the given calculation setup.
	 */
	public static MatrixData matrixData(IDatabase db, CalculationSetup setup) {
		return matrixData(db, setup, Collections.emptyMap());
	}

	/**
	 * Create the matrix data for the given calculation setup and sub-results.
	 * The sub-results will be integrated into the resulting matrices.
	 */
	public static MatrixData matrixData(
			IDatabase db,
			CalculationSetup setup,
			Map<ProcessProduct, SimpleResult> subResults) {

		var techIndex = createProductIndex(setup.productSystem, db);
		techIndex.setDemand(setup.getDemandValue());
		var interpreter = interpreter(db, setup, techIndex);

		var conf = new InventoryConfig(db, techIndex);
		conf.allocationMethod = setup.allocationMethod;
		conf.interpreter = interpreter;
		conf.subResults = subResults;
		conf.withCosts = setup.withCosts;
		conf.withRegionalization = setup.withRegionalization;
		conf.withUncertainties = setup.withUncertainties;
		var builder = new InventoryBuilder(conf);
		var data = builder.build();

		// add the LCIA matrix structures
		if (setup.impactMethod != null) {
			var impactIdx = new ImpactIndex();
			new ImpactMethodDao(db).getCategoryDescriptors(
					setup.impactMethod.id).forEach(impactIdx::put);
			if (!impactIdx.isEmpty()) {
				var impactBuilder = new ImpactBuilder(db);
				impactBuilder.withUncertainties(conf.withUncertainties);
				var impactData = impactBuilder.build(
						data.flowIndex, impactIdx, interpreter);
				data.impactMatrix = impactData.impactMatrix;
				data.impactIndex = impactIdx;
				data.impactUncertainties = impactData.impactUncertainties;
			}
		}

		return data;
	}

	public static FormulaInterpreter interpreter(IDatabase db,
			CalculationSetup setup, TechIndex techIndex) {
		// collect the process and LCIA category IDs; these
		// are the possible contexts of local parameters
		HashSet<Long> contexts = new HashSet<>();
		if (techIndex != null) {
			contexts.addAll(techIndex.getProcessIds());
		}
		if (setup.impactMethod != null) {
			ImpactMethodDao dao = new ImpactMethodDao(db);
			dao.getCategoryDescriptors(setup.impactMethod.id).forEach(
					d -> contexts.add(d.id));
		}
		return ParameterTable.interpreter(
				db, contexts, setup.parameterRedefs);
	}
}
