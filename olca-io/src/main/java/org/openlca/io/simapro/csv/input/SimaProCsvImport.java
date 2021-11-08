package org.openlca.io.simapro.csv.input;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.io.FileImport;
import org.openlca.io.maps.FlowMap;
import org.openlca.simapro.csv.CsvDataSet;
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

	public SimaProCsvImport(IDatabase db, File... files) {
		this.db = db;
		this.files = files;
	}

	public SimaProCsvImport withFlowMap(FlowMap flowMap) {
		this.flowMap = flowMap;
		return this;
	}

	public SimaProCsvImport unrollWasteScenarios(boolean b) {
		this.unrollWasteScenarios = b;
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

	@Override
	public void run() {
		if (files == null || files.length == 0)
			return;

		var flowMap = this.flowMap == null
			? FlowMap.empty()
			: this.flowMap;
		var refData = new RefData(db, flowMap);

		try {
			for (File file : files) {
				log.trace("import SimaPro CSV file {}", file);
				log.trace("extract reference data");

				var dataSet = CsvDataSet.read(file);
				if (unrollWasteScenarios) {
					WasteScenarios.unroll(dataSet);
				}

				// reference data
				refData.sync(dataSet);

				// processes
				for (var process : dataSet.processes()) {
					Processes.map(db, refData, process);
				}

				// product stages
				for (var stage : dataSet.productStages()) {
					ProductStages.map(db, refData, stage);
				}

				// impact methods
				for (var method : dataSet.methods()) {
					ImpactMethods.map(db ,refData, method);
				}
			}
		} catch (Exception e) {
			log.error("SimaPro CSV import failed");
		}
	}

}
