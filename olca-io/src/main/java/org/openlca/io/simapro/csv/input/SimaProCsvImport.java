package org.openlca.io.simapro.csv.input;

import java.io.File;
import java.util.ArrayList;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.ProductSystemBuilder;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.linking.LinkingConfig;
import org.openlca.core.matrix.linking.ProviderLinking;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.io.FileImport;
import org.openlca.io.maps.FlowMap;
import org.openlca.simapro.csv.SimaProCsv;
import org.openlca.simapro.csv.enums.ProductStageCategory;
import org.openlca.simapro.csv.process.ProductStageBlock;
import org.openlca.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class SimaProCsvImport implements FileImport {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private boolean canceled = false;
	private final IDatabase db;
	private final File[] files;
	private EventBus eventBus;
	private FlowMap flowMap;
	private boolean unrollWasteScenarios;
	private boolean expandImpactFactors;
	private boolean generateLifeCycleSystems;

	public SimaProCsvImport(IDatabase db, File... files) {
		this.db = db;
		this.files = files;
	}

	// region config
	public SimaProCsvImport withFlowMap(FlowMap flowMap) {
		this.flowMap = flowMap;
		return this;
	}

	public SimaProCsvImport unrollWasteScenarios(boolean b) {
		this.unrollWasteScenarios = b;
		return this;
	}

	public SimaProCsvImport expandImpactFactors(boolean b) {
		this.expandImpactFactors = b;
		return this;
	}

	/**
	 * If set to {@code true} the import will generate product systems for life
	 * cycle stages of type {@code 'life cycle'}.
	 */
	public SimaProCsvImport generateLifeCycleSystems(boolean b) {
		this.generateLifeCycleSystems = b;
		return this;
	}

	public EventBus getEventBus() {
		return eventBus;
	}

	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public void cancel() {
		this.canceled = true;
	}

	@Override
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}
	// endregion

	@Override
	public void run() {
		if (files == null || files.length == 0)
			return;

		var flowMap = this.flowMap == null
			? FlowMap.empty()
			: this.flowMap;
		var contexts = ImportContext.of(db, flowMap);

		try {
			for (File file : files) {
				log.trace("import SimaPro CSV file {}", file);
				log.trace("extract reference data");

				var dataSet = SimaProCsv.read(file);
				if (unrollWasteScenarios) {
					WasteScenarios.unroll(dataSet);
				}
				if (expandImpactFactors) {
					for (var method : dataSet.methods()) {
						for (var impact : method.impactCategories()) {
							ImpactFactors.expand(impact);
						}
					}
				}

				// reference data
				var context = contexts.next(dataSet);

				// processes
				for (var process : dataSet.processes()) {
					Processes.map(context, process);
				}

				// product stages and life cycle systems
				var lifeCycles = new ArrayList<Pair<ProductStageBlock, Process>>();
				for (var stage : dataSet.productStages()) {
					var process = ProductStages.map(context, stage);
					if (generateLifeCycleSystems
						&& process.isPresent()
						&& stage.category() == ProductStageCategory.LIFE_CYCLE) {
						lifeCycles.add(Pair.of(stage, process.get()));
					}
				}
				for (var pair : lifeCycles) {
					createSystemOf(pair.first, pair.second);
				}

				// impact methods
				for (var method : dataSet.methods()) {
					ImpactMethods.map(db, context.refData(), method);
				}
			}
		} catch (Exception e) {
			log.error("SimaPro CSV import failed");
		}
	}

	private void createSystemOf(ProductStageBlock block, Process process) {
		var linkingConfig = new LinkingConfig()
			.providerLinking(ProviderLinking.PREFER_DEFAULTS)
			.preferredType(ProcessType.LCI_RESULT);
		var system = new ProductSystemBuilder(
			MatrixCache.createLazy(db), linkingConfig).build(process);
		var params = wasteScenarioParamOf(block);
		if (params != null) {
			system.parameterSets.add(params);
		}
		db.insert(system);
	}

	private ParameterRedefSet wasteScenarioParamOf(ProductStageBlock block) {
		var ws = block.wasteOrDisposalScenario();
		if (ws == null)
			return null;
		var param = WasteScenarios.parameterOf(ws.name());
		var global = db.forName(Parameter.class, param);
		if (global == null)
			return null;
		var paramSet = new ParameterRedefSet();
		paramSet.name = "Parameters";
		paramSet.isBaseline = true;
		var redef = new ParameterRedef();
		redef.name = param;
		redef.value = 1;
		redef.description = "Set to 1 to enable waste scenario '"
			+ ws.name() + "'. Set it to 0 to disable it.";
		paramSet.parameters.add(redef);
		return paramSet;
	}
}
