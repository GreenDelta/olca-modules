package org.openlca.core.matrix;

import java.io.File;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.CalculationType;
import org.openlca.core.math.DataStructures;
import org.openlca.core.math.LcaCalculator;
import org.openlca.core.math.ReferenceAmount;
import org.openlca.core.matrix.cache.ExchangeTable;
import org.openlca.core.matrix.cache.FlowTable;
import org.openlca.core.matrix.format.MatrixBuilder;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.FullResult;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.julia.Julia;
import org.openlca.julia.JuliaSolver;

import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * A fast matrix builder that skips the product system linking layer ...
 */
public class FastMatrixBuilder {

	private final IDatabase db;
	private final CalculationSetup setup;
	private final FlowTable flows;

	private AllocationIndex allocationIndex;
	private TechIndex techIndex;
	private FlowIndex flowIndex;

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
	}

	public MatrixData build() {
		techIndex = buildTechIndex();
		flowIndex = new FlowIndex();
		interpreter = DataStructures.interpreter(
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

		ExchangeTable exchanges = new ExchangeTable(db);
		exchanges.each(techIndex, exchange -> {
			List<ProcessProduct> products = techIndex
					.getProviders(exchange.processId);
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
		data.enviIndex = flowIndex;
		data.techMatrix = techBuilder.finish();
		data.enviMatrix = enviBuilder.finish();

		// TODO: LCIA matrices

		return data;
	}

	private void putExchangeValue(ProcessProduct provider, CalcExchange e) {
		if (e.flowType == FlowType.ELEMENTARY_FLOW) {
			// elementary flows
			addIntervention(provider, e);
			return;
		}

		if ((e.isInput && e.flowType == FlowType.PRODUCT_FLOW)
				|| (!e.isInput && e.flowType == FlowType.WASTE_FLOW)) {
			addProcessLink(provider, e);
			return;
		}

		if (provider.equals(e.processId, e.flowId)) {
			// the reference product or waste flow
			int idx = techIndex.getIndex(provider);
			add(idx, provider, techBuilder, e);
			return;
		}

		// TODO: other things should not happen here?
	}

	private void addProcessLink(ProcessProduct product, CalcExchange e) {
		ProcessProduct provider = null;
		if (e.defaultProviderId > 0) {
			provider = techIndex.getProvider(e.defaultProviderId, e.flowId);
		}
		if (provider == null) {
			if (providers == null) {
				providers = new TLongObjectHashMap<>();
				techIndex.each((i, pp) -> {
					providers.put(pp.flowId(), pp);
				});
			}
			provider = providers.get(e.flowId);
		}
		if (provider == null)
			return;
		int row = techIndex.getIndex(provider);
		add(row, product, techBuilder, e);
	}

	private void addIntervention(ProcessProduct provider, CalcExchange e) {
		int row = flowIndex.of(e.flowId);
		if (row < 0) {
			if (e.isInput) {
				row = flowIndex.putInput(flows.get(e.flowId));
			} else {
				row = flowIndex.putOutput(flows.get(e.flowId));
			}
		}
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
					provider, exchange.exchangeId);
		}
		matrix.add(row, col, exchange.matrixValue(
				interpreter, allocationFactor));
		if (setup.withCosts) {
			costs[col] += exchange.costValue(
					interpreter, allocationFactor);
		}
	}

	private TechIndex buildTechIndex() {
		// TODO: check for null-pointers
		ProcessProduct qref = ProcessProduct.of(
				setup.productSystem.referenceProcess,
				setup.productSystem.referenceExchange.flow);
		TechIndex idx = new TechIndex(qref);
		idx.setDemand(ReferenceAmount.get(setup));

		TLongObjectHashMap<ProcessDescriptor> processes = new ProcessDao(
				db).descriptorMap();

		String sql = "select f_owner, f_flow, is_input from tbl_exchanges";
		try {
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
				ProcessProduct pp = ProcessProduct.of(
						processes.get(procID), flows.get(flowID));
				idx.put(pp);
				return true;
			});
		} catch (Exception e) {
			throw new RuntimeException("failed to build tech-index", e);
		}
		return idx;
	}

	public static void main(String[] args) {
		System.out.println("load ps");
		String workspace = "C:/Users/ms/openLCA-data-1.4";
		String dbPath = workspace
				+ "/databases/zimple";
		IDatabase db = new DerbyDatabase(new File(dbPath));
		ProductSystem system = new ProductSystemDao(db).getForRefId(
				"8a42a5d5-7244-4692-a735-067eeedbc710");
		CalculationSetup setup = new CalculationSetup(
				CalculationType.CONTRIBUTION_ANALYSIS, system);

		System.out.println("build it");
		long start = System.currentTimeMillis();

		FastMatrixBuilder builder = new FastMatrixBuilder(db, setup);
		MatrixData data = builder.build();

		long end = System.currentTimeMillis();
		double time = (end - start) / 1000.0;
		System.out.println("matrix build took " + time + " secs");

		// now the full result calculation
		start = System.currentTimeMillis();
		String juliaLibPath = "C:\\Users\\ms\\Projects\\openLCA\\eclipse";
		Julia.loadFromDir(new File(juliaLibPath));
		JuliaSolver solver = new JuliaSolver();
		LcaCalculator calc = new LcaCalculator(solver, data);
		FullResult r = calc.calculateFull();
		end = System.currentTimeMillis();
		time = (end - start) / 1000.0;
		System.out.println("calculation took " + time + " secs");

		System.out.println("done; flow count = " + r.flowIndex.size());
		System.out.println(r.totalFlowResults[0]);

	}

}
