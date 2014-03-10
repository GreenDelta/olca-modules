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
	private final File file;
	private EventBus eventBus;

	public SimaProCsvImport(IDatabase database, File file) {
		this.database = database;
		this.file = file;
	}

	public IDatabase getDatabase() {
		return database;
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
		log.trace("import SimaPro CSV file {}", file);
		try {
			log.trace("sync. reference data");
			RefDataHandler refDataHandler = new RefDataHandler(this);
			SimaProCSV.parse(file, refDataHandler);
			refDataHandler.finish();
			log.trace("import processes");
			ProcessHandler processHandler = new ProcessHandler(database,
					refDataHandler.getRefData());
			SimaProCSV.parse(file, processHandler);
		} catch (Exception e) {
			log.error("SimaPro CSV import failed");
		}
	}

}
