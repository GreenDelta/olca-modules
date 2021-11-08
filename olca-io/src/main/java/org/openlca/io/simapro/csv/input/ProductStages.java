package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
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

	static void map(IDatabase db, RefData refData, ProductStageBlock block) {
		new ProductStages(db, refData, block).exec();
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

	void exec() {

		var name = !block.products().isEmpty()
			? block.products().get(0).name()
			: "";
		var type = block.category() != null
			? block.category().name()
			: "";
		var refId = KeyGen.get("SimaPro CSV", type, name);

		process = db.get(Process.class, refId);
		if (process != null) {
			log.warn("A process with id={} already exists", refId);
			return;
		}

		// meta-data
		process = new Process();
		process.refId = refId;
		process.name = name;
		process.processType = ProcessType.UNIT_PROCESS;
		if (Strings.notEmpty(type)) {
			process.tags = type;
		}
		process.category = categoryOf(block);
		formulaScope = ProcessParameters.map(db, this);

		// product outputs
		for (var row : block.products()) {
			var flow = refData.productOf(row);
			var e = exchangeOf(flow, row);
			if (e == null)
				continue;
			e.isInput = false;
		}

		// product inputs
		Consumer<TechExchangeRow> input = row -> {
			if (row == null)
				return;
			var flow = flowMap.productOf(row);
			var exchange = exchangeOf(process, flow, row);
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

		db.insert(process);
	}

	private Category categoryOf(ProductStageBlock block) {
		if (block == null)
			return null;

		var path = new ArrayList<String>();

		if (block.category() != null) {
			var root = block.category().toString();
			if (root.length() > 0) {
				path.add(
					root.substring(0, 1).toUpperCase() + root.substring(1));
			}
		}

		if (!block.products().isEmpty()) {
			var product = block.products().get(0);
			var c = product.category();
			if (Strings.notEmpty(c)) {
				path.addAll(Arrays.asList(c.split("\\\\")));
			}
		}

		return path.isEmpty()
			? null
			: CategoryDao.sync(db, ModelType.PROCESS, path.toArray(String[]::new));
	}

}
