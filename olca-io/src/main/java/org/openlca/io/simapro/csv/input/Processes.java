package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.UUID;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.expressions.Scope;
import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.simapro.csv.enums.ProcessType;
import org.openlca.simapro.csv.enums.ProductType;
import org.openlca.simapro.csv.process.ExchangeRow;
import org.openlca.simapro.csv.process.ProcessBlock;
import org.openlca.util.Exchanges;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Processes {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final IDatabase db;
	private final RefData refData;
	private final ProcessBlock block;

	private Process process;
	private Scope formulaScope;

	private Processes(IDatabase db, RefData refData, ProcessBlock block) {
		this.db = db;
		this.refData = refData;
		this.block = block;
	}

	static void map(IDatabase db, RefData refData, ProcessBlock block) {
		new Processes(db, refData, block).exec();
	}

	private void exec() {
		var refId = Strings.notEmpty(block.identifier())
			? KeyGen.get(block.identifier())
			: UUID.randomUUID().toString();
		var process = db.get(Process.class, refId);
		if (process != null) {
			log.warn("a process with the identifier {} is already in the "
				+ "database and was not imported", refId);
			return;
		}

		log.trace("import process {}", refId);
		process = new Process();
		process.refId = refId;
		process.processType = block.processType() == ProcessType.SYSTEM
			? org.openlca.core.model.ProcessType.LCI_RESULT
			: org.openlca.core.model.ProcessType.UNIT_PROCESS;
		process.name = mapName();
		process.defaultAllocationMethod = AllocationMethod.PHYSICAL;
		ProcessDocs.map(refData, block, process);

		this.process = process;
		formulaScope = ProcessParameters.map(db, block, process);

		mapExchanges();
		mapAllocation();

		// take the location and category from the reference flow
		if (process.quantitativeReference != null) {
			var f = process.quantitativeReference.flow;
			process.location = f.location;
			if (f.category != null) {
				var path = new ArrayList<String>();
				var c = f.category;
				while (c != null) {
					path.add(0, c.name);
					c = c.category;
				}
				process.category = CategoryDao.sync(
					db, ModelType.PROCESS, path.toArray(String[]::new));
			}
		}

		db.insert(process);
	}

	private String mapName() {
		if (Strings.notEmpty(block.name()))
			return block.name();
		if (!block.products().isEmpty())
			return block.products().get(0).name();
		if (block.wasteTreatment() != null)
			return block.wasteTreatment().name();
		return block.identifier();
	}


	private void mapAllocation() {
		if (block.products().size() < 2)
			return;
		for (var output : block.products()) {
			var flow = refData.productOf(output);
			if (flow == null || flow.flow() == null)
				continue;

			// prepare the template of the factor
			var f = new AllocationFactor();
			f.productId = flow.flow().id;
			var value = output.allocation();
			f.value = ProcessParameters.eval(formulaScope, value);
			if (value.hasFormula()) {
				f.formula = value.formula();
			}

			// add the physical factor
			var physical = f.copy();
			physical.method = AllocationMethod.PHYSICAL;
			process.allocationFactors.add(physical);

			// add the economic factor
			var economic = f.copy();
			economic.method = AllocationMethod.ECONOMIC;
			process.allocationFactors.add(economic);

			// add causal factors
			for (var e : process.exchanges) {
				if (Exchanges.isProviderFlow(e))
					continue;
				var causal = f.copy();
				causal.method = AllocationMethod.CAUSAL;
				causal.exchange = e;
				process.allocationFactors.add(causal);
			}
		}
	}

	private void mapExchanges() {

		// reference products
		for (var row : block.products()) {
			var flow = refData.productOf(row);
			var e = exchangeOf(flow, row);
			if (e == null)
				continue;
			e.isInput = false;
			if (process.quantitativeReference == null) {
				process.quantitativeReference = e;
			}
		}

		// waste treatment
		if (block.wasteTreatment() != null) {
			var flow = refData.wasteFlowOf(block.wasteTreatment());
			var e = exchangeOf(flow, block.wasteTreatment());
			if (e != null) {
				e.isInput = true;
			}
		}

		// product inputs & waste outputs
		for (var type : ProductType.values()) {
			for (var row : block.exchangesOf(type)) {
				var flow = refData.productOf(row);
				var e = exchangeOf(flow, row);
				if (e == null)
					continue;
				e.isInput = type != ProductType.WASTE_TO_TREATMENT;
				e.isAvoided = type == ProductType.AVOIDED_PRODUCTS;
			}
		}

		// elementary flows
		for (var type : ElementaryFlowType.values()) {
			for (var row : block.exchangesOf(type)) {
				var flow = refData.elemFlowOf(type, row);
				var e = exchangeOf(flow, row);
				if (e == null)
					continue;
				e.isInput = type == ElementaryFlowType.RESOURCES;
			}
		}
	}

	private Exchange exchangeOf(SyncFlow f, ExchangeRow row) {
		if (f == null || f.flow() == null) {
			log.error(
				"could not create exchange as there was now flow found for {}", row);
			return null;
		}

		var e = new Exchange();
		process.lastInternalId++;
		e.internalId = process.lastInternalId;
		process.exchanges.add(e);
		e.description = row.comment();
		e.flow = f.flow();
		e.uncertainty = Uncertainties.of(row);

		if (f.isMapped()) {
			double factor = f.mapFactor();
			if (e.uncertainty != null) {
				e.uncertainty.scale(factor);
			}
			e.amount = factor * ProcessParameters.eval(formulaScope, row.amount());
			if (row.amount().hasFormula()) {
				e.formula = factor + " * (" + row.amount().formula() + ")";
			}
			// TODO: SyncFlow should store the mapped unit and flow property
			e.flowPropertyFactor = f.flow().getReferenceFactor();
			e.unit = f.flow().getReferenceUnit();

		} else {
			e.amount = ProcessParameters.eval(formulaScope, row.amount());
			if (row.amount().hasFormula()) {
				e.formula = row.amount().formula();
			}
			var quantity = refData.quantityOf(row.unit());
			if (quantity != null) {
				e.unit = quantity.unit;
				e.flowPropertyFactor = f.flow().getFactor(quantity.flowProperty);
			}
		}
		return e;
	}
}
