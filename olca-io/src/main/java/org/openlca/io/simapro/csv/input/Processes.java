package org.openlca.io.simapro.csv.input;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Process;
import org.openlca.expressions.Scope;
import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.simapro.csv.enums.ProcessType;
import org.openlca.simapro.csv.enums.ProductType;
import org.openlca.simapro.csv.process.ProcessBlock;
import org.openlca.simapro.csv.process.WasteTreatmentRow;
import org.openlca.simapro.csv.refdata.CalculatedParameterRow;
import org.openlca.simapro.csv.refdata.InputParameterRow;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Processes implements ProcessMapper {

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

	// region ProcessMapper
	@Override
	public IDatabase db() {
		return db;
	}

	@Override
	public Scope formulaScope() {
		return formulaScope;
	}

	@Override
	public RefData refData() {
		return refData;
	}

	@Override
	public List<CalculatedParameterRow> calculatedParameterRows() {
		return block.calculatedParameters();
	}

	@Override
	public List<InputParameterRow> inputParameterRows() {
		return block.inputParameters();
	}

	@Override
	public Process process() {
		return process;
	}
	// endregion

	private void exec() {
		var refId = Strings.notEmpty(block.identifier())
			? KeyGen.get(block.identifier())
			: UUID.randomUUID().toString();
		process = db.get(Process.class, refId);
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
		process.name = nameOf(block);
		process.defaultAllocationMethod = AllocationMethod.PHYSICAL;
		ProcessDocs.map(refData, block, process);
		formulaScope = ProcessParameters.map(this);

		mapExchanges();
		mapAllocation();
		inferCategoryAndLocation();
		db.insert(process);
	}

	static String nameOf(ProcessBlock block) {
		if (!block.products().isEmpty())
			return block.products().get(0).name();
		if (block.wasteTreatment() != null)
			return block.wasteTreatment().name();
		if (block.wasteScenario() != null)
			return block.wasteScenario().name();
		return Strings.notEmpty(block.name())
			? block.name()
			: block.identifier();
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
				if (org.openlca.util.Exchanges.isProviderFlow(e))
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
			var e = Exchanges.of(this, flow, row);
			if (e == null)
				continue;
			e.isInput = false;
			if (process.quantitativeReference == null) {
				process.quantitativeReference = e;
			}
		}

		// waste treatment or waste scenario
		Consumer<WasteTreatmentRow> waste = row -> {
			if (row == null)
				return;
			var flow = refData.wasteFlowOf(row);
			var e = Exchanges.of(this, flow, row);
			if (e != null) {
				e.isInput = true;
				if (process.quantitativeReference == null) {
					process.quantitativeReference = e;
				}
			}
		};
		waste.accept(block.wasteTreatment());
		waste.accept(block.wasteScenario());


		// product inputs & waste outputs
		for (var type : ProductType.values()) {
			for (var row : block.exchangesOf(type)) {
				var flow = refData.productOf(row);
				var e = Exchanges.of(this, flow, row);
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
				var e = Exchanges.of(this, flow, row);
				if (e == null)
					continue;
				e.isInput = type == ElementaryFlowType.RESOURCES;
			}
		}
	}

}