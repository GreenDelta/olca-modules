package org.openlca.core.matrix;

import java.util.HashSet;

import org.openlca.core.database.LocationDao;
import org.openlca.core.matrix.cache.ExchangeTable;
import org.openlca.core.matrix.cache.FlowTable;
import org.openlca.core.matrix.format.MatrixBuilder;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.uncertainties.UMatrix;
import org.openlca.core.model.descriptors.LocationDescriptor;

import gnu.trove.map.hash.TLongObjectHashMap;

public class InventoryBuilder {

	private final MatrixConfig conf;
	private final TechIndex techIndex;
	private final FlowTable flows;
	private final EnviIndex flowIndex;

	private final TLongObjectHashMap<LocationDescriptor> locations;
	private final AllocationIndex allocationIndex;

	private final MatrixBuilder techBuilder;
	private final MatrixBuilder enviBuilder;
	private UMatrix techUncerts;
	private UMatrix enviUncerts;
	private double[] costs;

	public InventoryBuilder(MatrixConfig conf) {
		this.conf = conf;

		// setup the indices
		this.techIndex = conf.techIndex;
		this.flows = FlowTable.create(conf.db);
		locations = conf.withRegionalization
			? new LocationDao(conf.db).descriptorMap()
			: null;
		allocationIndex = conf.hasAllocation()
			? AllocationIndex.create(conf)
			: null;

		// create the index of elementary flows; when the system has sub-systems
		// we add the flows of the sub-systems to the index; note that there
		// can be elementary flows that only occur in a sub-system
		flowIndex = conf.withRegionalization
			? EnviIndex.createRegionalized()
			: EnviIndex.create();
		if (conf.subResults != null) {
			for (var subResult : conf.subResults.values()) {
				flowIndex.addAll(subResult.enviIndex());
			}
		}

		// create the matrix structures
		techBuilder = new MatrixBuilder();
		enviBuilder = new MatrixBuilder();
		if (conf.withUncertainties) {
			techUncerts = new UMatrix();
			enviUncerts = new UMatrix();
		}
		if (conf.withCosts) {
			costs = new double[conf.techIndex.size()];
		}
	}

	public MatrixData build() {

		// fill the matrices
		fillMatrices();
		int n = techIndex.size();
		int m = flowIndex.size();

		// create the matrix data
		var data = new MatrixData();
		data.demand = conf.demand;

		// product data
		data.techIndex = techIndex;
		techBuilder.minSize(n, n);
		data.techMatrix = techBuilder.finish();
		data.techUncertainties = techUncerts;

		// optional elementary flows
		if (m > 0) {
			data.enviIndex = flowIndex;
			enviBuilder.minSize(m, n);
			data.enviMatrix = enviBuilder.finish();
			data.enviUncertainties = enviUncerts;
		}

		// optional costs
		data.costVector = costs;
		return data;
	}

	private void fillMatrices() {
		// fill the matrices with process data
		var exchanges = new ExchangeTable(conf.db);
		exchanges.each(techIndex, exchange -> {
			var products = techIndex.getProviders(exchange.processId);
			for (TechFlow product : products) {
				putExchangeValue(product, exchange);
			}
		});

		// now put the entries of the sub-system into the matrices
		var subSystems = new HashSet<TechFlow>();
		techIndex.each((i, p) -> {
			if (p.provider() == null || p.isProcess())
				return;
			subSystems.add(p);
		});
		if (subSystems.isEmpty())
			return;

		// use the MatrixBuilder.set method here because there may are stored LCI
		// results in the database (!) that were mapped to the same columns above
		for (TechFlow sub : subSystems) {

			int col = techIndex.of(sub);
			var result = conf.subResults.get(sub);
			if (result == null) {
				// TODO: log this error
				continue;
			}

			// add the link in the technology matrix
			double a = result.demand().value();
			techBuilder.set(col, col, a);

			// add the LCI result
			if (result.enviIndex() != null) {
				result.enviIndex().each((i, f) -> {
					double b = result.getTotalFlowResult(f);
					if (f.isInput()) {
						b = -b;
					}
					enviBuilder.set(flowIndex.of(f), col, b);
				});
			}

			// add costs
			if (conf.withCosts) {
				costs[col] = result.totalCosts();
			}
		}
	}

	private void putExchangeValue(TechFlow provider, CalcExchange e) {
		if (e.isElementary()) {
			// elementary flows
			addIntervention(provider, e);
			return;
		}

		if (e.isLinkable()) {
			var linkedProvider = conf.linker.providerOf(e);
			if (linkedProvider != null) {
				// linked product input or waste output
				int row = techIndex.of(linkedProvider);
				add(row, provider, techBuilder, e);
			} else {
				// unlinked product input or waste output
				addIntervention(provider, e);
			}
			return;
		}

		if (provider.matches(e.processId, e.flowId)) {
			// the reference product or waste flow
			int idx = techIndex.of(provider);
			add(idx, provider, techBuilder, e);
			return;
		}

		if (!conf.hasAllocation()) {
			// non-allocated output products or waste inputs
			addIntervention(provider, e);
		}
	}

	private void addIntervention(TechFlow provider, CalcExchange e) {
		int row = flowIndex.register(provider, e, flows, locations);
		add(row, provider, enviBuilder, e);
	}

	private void add(int row, TechFlow provider, MatrixBuilder matrix,
		CalcExchange exchange) {

		int col = techIndex.of(provider);
		if (row < 0 || col < 0)
			return;

		var allocationFactor = allocationIndex != null && exchange.isAllocatable()
			? allocationIndex.getFactor(provider, exchange.exchangeId)
			: null;
		var af = allocationFactor != null
			? allocationFactor.get(conf.interpreter)
			: 1;

		double value = exchange.matrixValue(conf.interpreter, af);
		matrix.add(row, col, value);

		if (conf.withCosts) {
			costs[col] += exchange.costValue(conf.interpreter, af);
		}

		if (conf.withUncertainties) {
			if (matrix == techBuilder) {
				techUncerts.add(row, col, exchange, allocationFactor);
			}
			if (matrix == enviBuilder) {
				enviUncerts.add(row, col, exchange, allocationFactor);
			}
		}
	}
}
