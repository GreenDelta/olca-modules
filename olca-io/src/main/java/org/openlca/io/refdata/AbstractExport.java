package org.openlca.io.refdata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

abstract class AbstractExport {

	protected Logger log = LoggerFactory.getLogger(getClass());

	public void run(File file, IDatabase database) {
		CsvPreference pref = new CsvPreference.Builder('"', ';', "\n").build();
		try (FileOutputStream fos = new FileOutputStream(file);
		     OutputStreamWriter writer = new OutputStreamWriter(fos, "utf-8");
		     BufferedWriter buffer = new BufferedWriter(writer);
		     CsvListWriter csvWriter = new CsvListWriter(buffer, pref)) {
			doIt(csvWriter, database);
		} catch (Exception e) {
			log.error("failed to write file " + file, e);
		}
	}

	protected abstract void doIt(CsvListWriter writer, IDatabase database)
			throws Exception;

}
