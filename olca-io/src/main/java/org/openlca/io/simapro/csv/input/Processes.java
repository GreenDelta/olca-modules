package org.openlca.io.simapro.csv.input;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.openlca.core.io.ImportLog;
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

class Processes implements ProcessMapper {


	private final ImportContext context;
	private final RefData refData;
	private final ImportLog log;
	private final ProcessBlock block;

	private Process process;
	private Scope formulaScope;

	private Processes(ImportContext context, ProcessBlock block) {
		this.context = context;
		this.refData = context.refData();
		this.log = context.log();
		this.block = block;
	}

	static void map(ImportContext context, ProcessBlock block) {
		new Processes(context, block).exec();
	}

	// region ProcessMapper
	@Override
	public ImportContext context() {
		return context;
	}

	@Override
	public Scope formulaScope() {
		return formulaScope;
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
		process = context.db().get(Process.class, refId);
		if (process != null) {
			log.warn("a process with the identifier '" + refId +
				"' is already in the database and was not imported");
			return;
		}

		process = new Process();
		process.refId = refId;
		process.processType = block.processType() == ProcessType.SYSTEM
			? org.openlca.core.model.ProcessType.LCI_RESULT
			: org.openlca.core.model.ProcessType.UNIT_PROCESS;
		process.name = nameOf(block);
		if (block.category() != null) {
			process.tags = block.category().toString();
		}
		process.defaultAllocationMethod = AllocationMethod.PHYSICAL;
		ProcessDocs.map(context.refData(), block, process);
		formulaScope = createFormulaScope();

		mapExchanges();
		mapAllocation();
		inferCategoryAndLocation();
		context.insert(process);
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
			f.value = eval(value);
			if (value.hasFormula()) {
				f.formula = formulaOf(value.formula());
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
			var e = exchangeOf(flow, row);
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
			var e = exchangeOf(flow, row);
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
			boolean isWaste = type == ProductType.WASTE_TO_TREATMENT;
			for (var row : block.exchangesOf(type)) {
				var flow = isWaste
					? refData.wasteFlowOf(row)
					: refData.productOf(row);
				var e = exchangeOf(flow, row);
				if (e == null)
					continue;
				e.isInput = !isWaste;
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

}
