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

	private Logger log = LoggerFactory.getLogger(getClass());

	private final IDatabase database;
	private final File xlsFile;

	public ExcelImport(File xlsFile, IDatabase database) {
		this.xlsFile = xlsFile;
		this.database = database;
	}

	@Override
	public void run() {
		log.trace("import file {}", xlsFile);
		try (FileInputStream fis = new FileInputStream(xlsFile)) {
			Workbook workbook = WorkbookFactory.create(fis);
			Process process = new Process();
			ProcessDocumentation doc = new ProcessDocumentation();
			process.setDocumentation(doc);
			Config config = new Config(workbook, database, process);
			readSheets(config);
			ProcessDao dao = new ProcessDao(database);
			dao.insert(process);
		} catch (Exception e) {
			log.error("failed to import file " + xlsFile, e);
		}
	}

	private void readSheets(Config config) {
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
		AllocationSheet.read(config);
	}
}
