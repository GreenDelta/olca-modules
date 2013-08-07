package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.indices.ExchangeTable;
import org.openlca.core.indices.FlowIndex;
import org.openlca.core.indices.LongPair;
import org.openlca.core.indices.ProductIndex;
import org.openlca.core.matrices.ImpactMatrix;
import org.openlca.core.matrices.ImpactMatrixBuilder;
import org.openlca.core.matrices.Inventory;
import org.openlca.core.matrices.InventoryBuilder;
import org.openlca.core.matrices.InventorySolver;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.results.AnalysisResult;
import org.openlca.core.results.InventoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemCalculator {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final IDatabase database;

	public SystemCalculator(IDatabase database) {
		this.database = database;
	}

	public InventoryResult solve(ProductSystem system) {
		return solve(system, null);
	}

	public InventoryResult solve(ProductSystem system,
			ImpactMethodDescriptor method) {
		log.trace("solve product system {}", system);
		ProductIndex index = buildProductIndex(system);
		Inventory inventory = buildInventory(index);
		log.trace("solve invenotory");
		InventorySolver solver = new InventorySolver();
		if (method == null)
			return solver.solve(inventory);
		else {
			ImpactMatrix impactMatrix = buildImpactMatrix(method,
					inventory.getFlowIndex());
			return solver.solve(inventory, impactMatrix);
		}
	}

	public AnalysisResult analyse(ProductSystem system) {
		return analyse(system, null);
	}

	public AnalysisResult analyse(ProductSystem system,
			ImpactMethodDescriptor method) {
		log.trace("analyse product system {}", system);
		ProductIndex index = buildProductIndex(system);
		Inventory inventory = buildInventory(index);
		log.trace("analyse inventory");
		InventorySolver solver = new InventorySolver();
		if (method == null)
			return solver.analyse(inventory);
		else {
			ImpactMatrix impactMatrix = buildImpactMatrix(method,
					inventory.getFlowIndex());
			return solver.analyse(inventory, impactMatrix);
		}
	}

	private Inventory buildInventory(ProductIndex index) {
		log.trace("build inventory");
		ExchangeTable exchangeTable = new ExchangeTable(database,
				index.getProcessIds());
		FlowIndex flowIndex = new FlowIndex(index, exchangeTable);
		InventoryBuilder inventoryBuilder = new InventoryBuilder(index,
				flowIndex, exchangeTable);
		Inventory inventory = inventoryBuilder.build();
		return inventory;
	}

	private ProductIndex buildProductIndex(ProductSystem system) {
		log.trace("create product index");
		Process refProcess = system.getReferenceProcess();
		Exchange refExchange = system.getReferenceExchange();
		Flow refFlow = refExchange.getFlow();
		LongPair refProduct = new LongPair(refProcess.getId(), refFlow.getId());
		double demand = system.getConvertedTargetAmount();
		ProductIndex index = new ProductIndex(refProduct, demand);
		for (ProcessLink link : system.getProcessLinks()) {
			long flow = link.getFlowId();
			long provider = link.getProviderProcessId();
			long recipient = link.getRecipientProcessId();
			LongPair processProduct = new LongPair(provider, flow);
			index.put(processProduct);
			LongPair input = new LongPair(recipient, flow);
			index.putLink(input, processProduct);
		}
		return index;
	}

	private ImpactMatrix buildImpactMatrix(ImpactMethodDescriptor method,
			FlowIndex flowIndex) {
		log.trace("build impact matrix for {}", method);
		ImpactMatrixBuilder builder = new ImpactMatrixBuilder(database);
		ImpactMatrix matrix = builder.build(method.getId(), flowIndex);
		return matrix;
	}
}
