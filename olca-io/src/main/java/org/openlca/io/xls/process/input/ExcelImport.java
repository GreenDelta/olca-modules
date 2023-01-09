package org.openlca.io.xls.process.input;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;

public class ExcelImport {

	private final IDatabase db;
	private final ImportLog log;

	public ExcelImport(IDatabase db) {
		this.db = db;
		this.log = new ImportLog();
	}

	public void next(File file) {
		try (var fis = new FileInputStream(file)) {
			var workbook = WorkbookFactory.create(fis);
			Process process = new Process();
			ProcessDocumentation doc = new ProcessDocumentation();
			process.documentation = doc;
			Config config = new Config(workbook, db, process);
			readSheets(config);
			ProcessDao dao = new ProcessDao(db);
			dao.insert(process);
		} catch (Exception e) {
			log.error("failed to import file");
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
