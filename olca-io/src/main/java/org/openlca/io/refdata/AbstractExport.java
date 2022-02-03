package org.openlca.io.refdata;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.IDatabase;
import org.openlca.io.maps.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractExport {

	protected Logger log = LoggerFactory.getLogger(getClass());

	public void run(File file, IDatabase database) {
		try (var writer = new FileWriter(file, StandardCharsets.UTF_8);
				 var printer = new CSVPrinter(writer, Maps.format())) {
			doIt(printer, database);
		} catch (Exception e) {
			log.error("failed to write file " + file, e);
		}
	}

	protected abstract void doIt(CSVPrinter printer, IDatabase database)
			throws Exception;

}
