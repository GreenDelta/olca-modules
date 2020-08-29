package org.openlca.io.xls.process.output;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exports one or more processes with related reference data to Excel into a
 * given file or folder. If only a single process is given and the given file is
 * not a folder and ends with '.xlsx', the result is directly written to that
 * file. Otherwise, the given file is used as folder and each process is written
 * to a separate Excel file.
 */
public class ExcelExport implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final File file;
	private final IDatabase db;
	private final List<ProcessDescriptor> descriptors;

	public ExcelExport(File file, IDatabase db,
			List<ProcessDescriptor> descriptors) {
		this.file = file;
		this.db = db;
		this.descriptors = descriptors;
	}

	@Override
	public void run() {
		try {
			ProcessDao dao = new ProcessDao(db);
			for (ProcessDescriptor d : descriptors) {
				Process p = dao.getForId(d.id);
				if (p == null) {
					log.warn("process {} was null; not exported", d);
					continue;
				}
				if (p.documentation == null) {
					// append a default documentation to avoid
					// null pointers later
					p.documentation =new ProcessDocumentation();
				}
				var wb = new SXSSFWorkbook();
				Config config = new Config(wb, db, p);
				writeSheets(config);
				File f = exportFile(p);
				try (FileOutputStream fos = new FileOutputStream(f)) {
					wb.write(fos);
				}
				wb.dispose();
			}
		} catch (Exception e) {
			log.error("failed to export process data sets to Excel", e);
		}
	}

	private File exportFile(Process p) {
		if (p == null)
			return null;
		if (!file.isDirectory()
				&& file.getName().toLowerCase().endsWith(".xlsx")
				&& descriptors.size() == 1)
			return file;
		if (!file.exists()) {
			file.mkdirs();
		}
		String name = p.refId + "_"
				+ Version.asString(p.version) + ".xlsx";
		return new File(file, name);
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
