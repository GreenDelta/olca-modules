package org.openlca.io.simapro.csv.input;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.io.FileImport;
import org.openlca.io.maps.FlowMap;
import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.SimaProCSV;
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

	public SimaProCsvImport(IDatabase db, File... files) {
		this.db = db;
		this.files = files;
	}

	public SimaProCsvImport with(FlowMap flowMap) {
		this.flowMap = flowMap;
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
		try {
			for (File file : files) {
				log.trace("import SimaPro CSV file {}", file);
				var dataSet = CsvDataSet.read(file);
				log.trace("extract reference data");

				var index = refDataHandler.getIndex();
				log.trace("sync. reference data");
				var flowMap = this.flowMap != null
						? this.flowMap
						: FlowMap.empty();
				var refData = new RefDataSync(index, db, flowMap).run();
				log.trace("import processes");
				var processHandler = new ProcessHandler(db, refData);
				SimaProCSV.parse(file, processHandler);
			}
		} catch (Exception e) {
			log.error("SimaPro CSV import failed");
		}
	}

}
