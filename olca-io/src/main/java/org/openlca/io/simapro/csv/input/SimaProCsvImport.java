package org.openlca.io.simapro.csv.input;

import java.io.File;
import java.util.ArrayList;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Process;
import org.openlca.io.Import;
import org.openlca.io.maps.FlowMap;
import org.openlca.simapro.csv.SimaProCsv;
import org.openlca.simapro.csv.enums.ProductStageCategory;
import org.openlca.simapro.csv.process.ProductStageBlock;
import org.openlca.util.Pair;

public class SimaProCsvImport implements Import {

	private final IDatabase db;
	private final File[] files;

	private FlowMap flowMap;
	private ImportLog _log;
	private boolean canceled = false;
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

	public SimaProCsvImport withLog(ImportLog log) {
		_log = log;
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

	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public ImportLog log() {
		if (_log == null) {
			_log = new ImportLog();
		}
		return _log;
	}

	@Override
	public void cancel() {
		this.canceled = true;
	}

	// endregion

	@Override
	public void run() {
		if (files == null || files.length == 0)
			return;

		var flowMap = this.flowMap == null
			? FlowMap.empty()
			: this.flowMap;
		var contexts = ImportContext.of(db, flowMap, log());

		try {
			for (File file : files) {
				log().info("import SimaPro CSV file: " + file.getName());

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
				ProductSystems.map(context, lifeCycles);

				// impact methods
				for (var method : dataSet.methods()) {
					ImpactMethods.map(context, method);
				}
			}
		} catch (Exception e) {
			log().error("SimaPro CSV import failed", e);
		}
	}

}
