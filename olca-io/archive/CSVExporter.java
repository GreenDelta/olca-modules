package org.openlca.io.csv.output;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPReferenceData;
import org.openlca.simapro.csv.writer.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVExporter implements Runnable {

	private CSVWriter writer;
	private ProcessConverter converter;
	private SPReferenceData referenceData;
	private ProcessDao processDao;
	private List<ProcessDescriptor> processes;
	private Logger log = LoggerFactory.getLogger(getClass());

	public CSVExporter(IDatabase database, File file, char separator,
			List<ProcessDescriptor> processes) throws IOException {
		converter = new ProcessConverter(database,
				(referenceData = new SPReferenceData()));
		writer = new CSVWriter(file);
		writer.setSeparator(separator);
		writer.setDecimalSeparator('.');
		writer.writeHeader("openLCA Export");
		processDao = new ProcessDao(database);
		this.processes = processes;
	}

	@Override
	public void run() {
		try {
			for (ProcessDescriptor descriptor : processes) {
				Process process = processDao.getForId(descriptor.getId());
				SPProcess spProcess = converter.convert(process);
				writer.write(spProcess);
			}
			writer.write(referenceData);
			writer.close();
		} catch (IOException e) {
			log.error("SimaPro CSV export failed", e);
		}
	}

}
