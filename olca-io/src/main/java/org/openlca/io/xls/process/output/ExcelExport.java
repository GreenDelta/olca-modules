package org.openlca.io.xls.process.output;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Process;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exports a set of processes and related reference data to Excel files in a
 * given directory.
 */
public class ExcelExport implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final File dir;
	private final IDatabase database;
	private final List<ProcessDescriptor> descriptors;

	public ExcelExport(File dir, IDatabase database,
			List<ProcessDescriptor> descriptors) {
		this.dir = dir;
		this.database = database;
		this.descriptors = descriptors;
	}

	@Override
	public void run() {
		try {
			if (!dir.exists())
				dir.mkdirs();
			ProcessDao dao = new ProcessDao(database);
			for (ProcessDescriptor descriptor : descriptors) {
				Process process = dao.getForId(descriptor.getId());
				if (process == null || process.getDocumentation() == null) {
					log.warn("process {} was null or has no documentation: "
							+ "not exported", descriptor);
					continue;
				}
				export(process);
			}
		} catch (Exception e) {
			log.error("failed to export process data sets to Excel", e);
		}
	}

	private void export(Process process) throws Exception {
		Workbook workbook = new SXSSFWorkbook();
		Config config = new Config(workbook, database, process);
		writeSheets(config);
		String fileName = process.getRefId() + "_"
				+ Version.asString(process.getVersion()) + ".xlsx";
		File file = new File(dir, fileName);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			workbook.write(fos);
		}
	}

	private void writeSheets(Config config) {
		InfoSheet.write(config);
		IOSheet.writeInputs(config);
		IOSheet.writeOutputs(config);
		ParameterSheet.write(config);
		AllocationSheet.write(config);
		ModelingSheet.write(config);
		AdminInfoSheet.write(config);
		// reference data
		FlowSheets.write(config);
		UnitSheet.write(config);
		UnitGroupSheet.write(config);
		FlowPropertySheet.write(config);
		ActorSheet.write(config);
		SourceSheet.write(config);
		LocationSheet.write(config);
	}
}
