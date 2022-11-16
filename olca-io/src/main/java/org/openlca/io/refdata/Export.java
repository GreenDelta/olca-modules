package org.openlca.io.refdata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.IDatabase;
import org.slf4j.LoggerFactory;

interface Export {

	default void run(File file, IDatabase db) {
		try (var writer = new FileWriter(file, StandardCharsets.UTF_8);
				 var printer = new CSVPrinter(writer, Csv.format())) {
			doIt(printer, db);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to write file " + file, e);
		}
	}

	void doIt(CSVPrinter printer, IDatabase db) throws IOException;

}
