package org.openlca.io.simapro.csv.input;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.io.FileImport;
import org.openlca.simapro.csv.SimaProCSV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class SimaProCsvImport implements FileImport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private boolean canceled = false;
	private final IDatabase database;
	private final File[] files;
	private EventBus eventBus;

	public SimaProCsvImport(IDatabase database, File[] files) {
		this.database = database;
		this.files = files;
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
				log.trace("extract reference data");
				SpRefIndexHandler refDataHandler = new SpRefIndexHandler();
				SimaProCSV.parse(file, refDataHandler);
				SpRefDataIndex index = refDataHandler.getIndex();
				log.trace("sync. reference data");
				RefDataSync sync = new RefDataSync(index, database);
				RefData refData = sync.run();
				log.trace("import processes");
				ProcessHandler processHandler = new ProcessHandler(database,
						refData);
				SimaProCSV.parse(file, processHandler);
			}
		} catch (Exception e) {
			log.error("SimaPro CSV import failed");
		}
	}

}
