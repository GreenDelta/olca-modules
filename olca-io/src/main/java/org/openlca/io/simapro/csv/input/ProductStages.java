package org.openlca.io.simapro.csv.input;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.expressions.Scope;
import org.openlca.simapro.csv.process.ProductStageBlock;
import org.openlca.simapro.csv.process.TechExchangeRow;
import org.openlca.simapro.csv.refdata.CalculatedParameterRow;
import org.openlca.simapro.csv.refdata.InputParameterRow;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProductStages implements ProcessMapper {

	private final Logger log = LoggerFactory.getLogger(ProductStages.class);
	private final IDatabase db;
	private final RefData refData;
	private final ProductStageBlock block;

	private Process process;
	private Scope formulaScope;

	private ProductStages(IDatabase db, RefData refData, ProductStageBlock block) {
		this.db = db;
		this.refData = refData;
		this.block = block;
	}

	/**
	 * Tries to import the given product stage as a process. Returns that process
	 * if it was created. If the process already exists in the database, an
	 * empty option is returned.
	 */
	static Optional<Process> map(
		IDatabase db, RefData refData, ProductStageBlock block) {
		var process = new ProductStages(db, refData, block).exec();
		return Optional.ofNullable(process);
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

		process = db.get(Process.class, refId);
		if (process != null) {
			log.warn("A process with id={} already exists", refId);
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
		formulaScope = ProcessParameters.map(this);

		mapExchanges();
		inferCategoryAndLocation();
		return db.insert(process);
	}

	private void mapExchanges() {
		// product outputs
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

		// product inputs
		Consumer<TechExchangeRow> input = row -> {
			if (row == null)
				return;
			var flow = refData.productOf(row);
			var exchange = Exchanges.of(this, flow, row);
			if (exchange == null)
				return;
			exchange.isInput = true;
		};
		var inputLists = List.of(
			block.materialsAndAssemblies(),
			block.processes(),
			block.disassemblies(),
			block.reuses(),
			block.additionalLifeCycles());
		for (var list : inputLists) {
			for (var row : list) {
				input.accept(row);
			}
		}
		input.accept(block.assembly());
	}
}
