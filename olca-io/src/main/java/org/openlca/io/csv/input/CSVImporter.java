package org.openlca.io.csv.input;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.io.FileImport;
import org.openlca.io.ImportEvent;
import org.openlca.simapro.csv.model.SPDataEntry;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPReferenceData;
import org.openlca.simapro.csv.model.SPWasteTreatment;
import org.openlca.simapro.csv.parser.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class CSVImporter implements FileImport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;
	private EventBus eventBus;
	private File files[];
	private boolean canceled = false;
	private ProcessImport processImporter;
	private ReferenceDataImporter dataImporter;

	public CSVImporter(IDatabase database) {
		this.database = database;
	}

	public CSVImporter(IDatabase database, File files[]) {
		this.database = database;
		this.files = files;
	}

	public void setFiles(File[] files) {
		this.files = files;
	}

	@Override
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	@Override
	public void cancel() {
		canceled = true;
	}

	@Override
	public void run() {
		for (File file : files) {
			if (canceled)
				break;
			CSVParser parser = new CSVParser(file);
			FormulaInterpreter interpreter = createWithGlobalParameter(database);
			dataImporter = new ReferenceDataImporter(database, interpreter);
			processImporter = new ProcessImport(database, interpreter);
			try {
				parser.start();
				SPReferenceData referenceData = parser.getReferenceData();
				CSVImportCache cache = dataImporter.importData(referenceData);
				processImporter.setCache(cache);
				while (parser.hasNext()) {
					SPDataEntry dataEntry = parser.next();
					fireEvent(dataEntry);
					if (dataEntry instanceof SPProcess)
						processImporter.runImport((SPProcess) dataEntry);
					else if (dataEntry instanceof SPWasteTreatment)
						processImporter.runImport((SPWasteTreatment) dataEntry);
					else
						log.debug("Unsupported Type: "
								+ dataEntry.getClass().getCanonicalName());
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void fireEvent(SPDataEntry dataEntry) {
		if (eventBus == null || dataEntry == null
				|| dataEntry.getDocumentation() == null
				|| dataEntry.getDocumentation().getName() == null)
			return;
		eventBus.post(new ImportEvent(dataEntry.getDocumentation().getName()));
	}

	private FormulaInterpreter createWithGlobalParameter(IDatabase database) {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		ParameterDao dao = new ParameterDao(database);
		for (Parameter parameter : dao.getGlobalParameters())
			if (parameter.isInputParameter())
				interpreter.getGlobalScope().bind(parameter.getName(),
						Double.toString(parameter.getValue()));
			else
				interpreter.getGlobalScope().bind(parameter.getName(),
						parameter.getFormula());
		return interpreter;
	}

}
