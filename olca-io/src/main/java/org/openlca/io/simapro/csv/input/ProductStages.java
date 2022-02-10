package org.openlca.io.simapro.csv.input;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.expressions.Scope;
import org.openlca.simapro.csv.enums.ProductStageCategory;
import org.openlca.simapro.csv.process.ProductStageBlock;
import org.openlca.simapro.csv.process.TechExchangeRow;
import org.openlca.simapro.csv.refdata.CalculatedParameterRow;
import org.openlca.simapro.csv.refdata.InputParameterRow;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;

class ProductStages implements ProcessMapper {

	private final ImportContext context;
	private final RefData refData;
	private final ImportLog log;
	private final ProductStageBlock block;

	private Process process;
	private Scope formulaScope;

	private ProductStages(ImportContext context, ProductStageBlock block) {
		this.context = context;
		this.refData = context.refData();
		this.log = context.log();
		this.block = block;
	}

	/**
	 * Tries to import the given product stage as a process. Returns that process
	 * if it was created. If the process already exists in the database, an
	 * empty option is returned.
	 */
	static Optional<Process> map(ImportContext context, ProductStageBlock block) {
		var process = new ProductStages(context, block).exec();
		return Optional.ofNullable(process);
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
	public Process process() {
		return process;
	}

	@Override
	public List<InputParameterRow> inputParameterRows() {
		return block.inputParameters();
	}

	@Override
	public List<CalculatedParameterRow> calculatedParameterRows() {
		return block.calculatedParameters();
	}
	// endregion

	private Process exec() {

		var name = !block.products().isEmpty()
			? block.products().get(0).name()
			: "";
		var type = block.category() != null
			? block.category().toString()
			: "";
		var refId = KeyGen.get("SimaPro CSV", type, name);

		process = context.db().get(Process.class, refId);
		if (process != null) {
			log.warn("A process with id='" + refId + "' already exists; skipped");
			return null;
		}

		// meta-data
		process = new Process();
		process.refId = refId;
		process.name = name;
		process.processType = ProcessType.UNIT_PROCESS;
		if (Strings.notEmpty(type)) {
			process.tags = type;
		}
		formulaScope = createFormulaScope();

		mapExchanges();
		inferCategoryAndLocation();
		return context.insert(process);
	}

	private void mapExchanges() {
		// product outputs
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

		// product inputs
		Function<TechExchangeRow, Exchange> input = row -> {
			if (row == null)
				return null;
			var flow = refData.productOf(row);
			var exchange = exchangeOf(flow, row);
			if (exchange == null)
				return null;
			exchange.isInput = true;
			return exchange;
		};
		var inputLists = List.of(
			block.materialsAndAssemblies(),
			block.processes(),
			block.disassemblies(),
			block.reuses(),
			block.additionalLifeCycles());
		for (var list : inputLists) {
			for (var row : list) {
				input.apply(row);
			}
		}
		input.apply(block.assembly());

		// map reuse of products as avoided product
		if (block.category() == ProductStageCategory.REUSE) {
			var exchange = input.apply(block.referenceAssembly());
			if (exchange != null) {
				exchange.isAvoided = true;
			}
		}

	}
}
