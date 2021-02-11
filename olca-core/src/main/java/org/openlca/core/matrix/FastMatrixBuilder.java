package org.openlca.core.matrix;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.matrix.cache.ExchangeTable;
import org.openlca.core.matrix.cache.FlowTable;
import org.openlca.core.matrix.format.MatrixBuilder;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.expressions.FormulaInterpreter;

import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * A fast matrix builder that skips the product system linking layer ...
 *
 * @deprecated it is now possible to build inventory matrices of a complete
 * database also with the InventoryBuilder. The performance is the same and it
 * has more features (like changing the linker or collecting uncertainty data
 * in the same table scan).
 */
@Deprecated
public class FastMatrixBuilder {

	private final IDatabase db;
	private final CalculationSetup setup;
	private TechIndex techIndex;
	private final FlowTable flows;
	private FlowIndex flowIndex;

	private final TLongObjectHashMap<LocationDescriptor> locations;
	private AllocationIndex allocationIndex;

	private FormulaInterpreter interpreter;
	private MatrixBuilder techBuilder;
	private MatrixBuilder enviBuilder;
	private double[] costs;

	/**
	 * A map that assigns the IDs of products and waste flows to their
	 * respective providers. This map is initialized lazily when there are no
	 * default providers on product inputs or waste outputs. In this case, this
	 * matrix builder only works correctly when each product (waste) is only
	 * produced (treated) by a single process in the database.
	 */
	private TLongObjectHashMap<ProcessProduct> providers;

	public FastMatrixBuilder(IDatabase db, CalculationSetup setup) {
		this.db = db;
		this.setup = setup;
		this.flows = FlowTable.create(db);
		locations = setup.withRegionalization
			? new LocationDao(db).descriptorMap()
			: null;

	}

	public MatrixData build() {
		techIndex = buildTechIndex();
		flowIndex = setup.withRegionalization
			? FlowIndex.createRegionalized()
			: FlowIndex.create();
		interpreter = MatrixData.interpreter(
			db, setup, techIndex);
		techBuilder = new MatrixBuilder();
		enviBuilder = new MatrixBuilder();

		if (setup.allocationMethod != null
				&& setup.allocationMethod != AllocationMethod.NONE) {
			allocationIndex = AllocationIndex.create(
				db, techIndex, setup.allocationMethod);
		}
		if (setup.withCosts) {
			costs = new double[techIndex.size()];
		}

		var exchanges = new ExchangeTable(db);
		exchanges.each(techIndex, exchange -> {
			var products = techIndex.getProviders(exchange.processId);
			for (ProcessProduct product : products) {
				putExchangeValue(product, exchange);
			}
		});

		int n = techIndex.size();
		int m = flowIndex.size();
		techBuilder.minSize(n, n);
		enviBuilder.minSize(m, n);

		MatrixData data = new MatrixData();
		data.techIndex = techIndex;
		data.flowIndex = flowIndex;
		data.techMatrix = techBuilder.finish();
		data.flowMatrix = enviBuilder.finish();
		data.costVector = costs;

		// add LCIA matrices
		if (setup.impactMethod != null) {
			addImpacts(data);
		}

		return data;
	}

	private void addImpacts(MatrixData data) {
		if (flowIndex.isEmpty())
			return;

		// load the LCIA category index
		var dao = new ImpactMethodDao(db);
		var impacts = dao.getCategoryDescriptors(setup.impactMethod.id);
		if (impacts.isEmpty())
			return;
		var impactIndex = ImpactIndex.of(impacts);

		// build the matrix and add it to the data
		new ImpactBuilder(db)
			.build(flowIndex, impactIndex, interpreter)
			.addTo(data);
	}

	private void putExchangeValue(ProcessProduct provider, CalcExchange e) {
		if (e.isElementary()) {
			// elementary flows
			addIntervention(provider, e);
			return;
		}

		if (e.isLinkable()) {
			addProcessLink(provider, e);
			return;
		}

		if (provider.equals(e.processId, e.flowId)) {
			// the reference product or waste flow
			int idx = techIndex.getIndex(provider);
			add(idx, provider, techBuilder, e);
		}
	}

	private void addProcessLink(ProcessProduct product, CalcExchange e) {
		ProcessProduct provider = null;
		if (e.defaultProviderId > 0) {
			provider = techIndex.getProvider(e.defaultProviderId, e.flowId);
		}
		if (provider == null) {
			if (providers == null) {
				providers = new TLongObjectHashMap<>();
				techIndex.each(
					(i, pp) -> providers.put(pp.flowId(), pp));
			}
			provider = providers.get(e.flowId);
		}
		if (provider == null)
			return;
		int row = techIndex.getIndex(provider);
		add(row, product, techBuilder, e);
	}

	private void addIntervention(ProcessProduct provider, CalcExchange e) {
		int row = flowIndex.register(provider, e, flows, locations);
		add(row, provider, enviBuilder, e);
	}

	private void add(int row, ProcessProduct provider, MatrixBuilder matrix,
									 CalcExchange exchange) {
		int col = techIndex.getIndex(provider);
		if (row < 0 || col < 0)
			return;

		double allocationFactor = 1.0;
		if (allocationIndex != null && exchange.isAllocatable()) {
			allocationFactor = allocationIndex.get(
				provider, exchange.exchangeId, interpreter);
		}
		matrix.add(row, col, exchange.matrixValue(
			interpreter, allocationFactor));
		if (setup.withCosts) {
			costs[col] += exchange.costValue(
				interpreter, allocationFactor);
		}
	}

	private TechIndex buildTechIndex() {
		ProcessProduct qref = ProcessProduct.of(
			setup.productSystem.referenceProcess,
			setup.productSystem.referenceExchange.flow);
		TechIndex idx = new TechIndex(qref);
		idx.setDemand(setup.getDemandValue());

		var processes = new ProcessDao(db).descriptorMap();

		String sql = "select f_owner, f_flow, is_input from tbl_exchanges";
		NativeSql.on(db).query(sql, r -> {
			long flowID = r.getLong(2);
			FlowType type = flows.type(flowID);
			if (type == FlowType.ELEMENTARY_FLOW)
				return true;
			boolean isInput = r.getBoolean(3);
			if (isInput && type == FlowType.PRODUCT_FLOW)
				return true;
			if (!isInput && type == FlowType.WASTE_FLOW)
				return true;
			long procID = r.getLong(1);
			var process = processes.get(procID);
			var flow = flows.get(flowID);
			if (process == null || flow == null) {
				// note that product system results could be
				// stored in the exchanges table; in this
				// case the process would be null.
				return true;
			}
			idx.put(ProcessProduct.of(process, flow));
			return true;
		});
		return idx;
	}
}
