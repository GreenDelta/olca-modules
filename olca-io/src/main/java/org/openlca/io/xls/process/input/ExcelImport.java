package org.openlca.io.xls.process.input;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelImport implements Runnable {

	private final IDatabase database;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final File xlsFile;

	public ExcelImport(final File xlsFile, final IDatabase database) {
		this.xlsFile = xlsFile;
		this.database = database;
	}

	private void readSheets(final Config config) {
		// reference data
		LocationSheet.read(config);
		ActorSheet.read(config);
		SourceSheet.read(config);
		UnitSheets.read(config);
		FlowSheets.read(config);
		// process sheets
		IOSheet.readInputs(config);
		IOSheet.readOutputs(config);
		InfoSheet.read(config); // after exchanges! find qRef
		AdminInfoSheet.read(config);
		ModelingSheet.read(config);
		ParameterSheet.read(config);
	}

	@Override
	public void run() {
		log.trace("import file {}", xlsFile);
		try (FileInputStream fis = new FileInputStream(xlsFile)) {
			final Workbook workbook = WorkbookFactory.create(fis);
			final Process process = new Process();
			final ProcessDocumentation doc = new ProcessDocumentation();
			process.setDocumentation(doc);
			final Config config = new Config(workbook, database, process);
			readSheets(config);
			final ProcessDao dao = new ProcessDao(database);
			dao.insert(process);
		} catch (final Exception e) {
			log.error("failed to import file " + xlsFile, e);
		}
	}
}
