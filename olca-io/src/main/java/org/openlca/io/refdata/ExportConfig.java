package org.openlca.io.refdata;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.RefEntity;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

record ExportConfig(File dir, IDatabase db) {

	static ExportConfig of(File dir, IDatabase db) {
		return new ExportConfig(dir, db);
	}

	void writeTo(String file, CsvWriter writer) {
		var f = new File(dir, file);
		try (var w = new FileWriter(f, StandardCharsets.UTF_8);
				 var p = new CSVPrinter(w, Csv.format())) {
			writer.writeTo(p);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to write file " + file, e);
		}
	}

	void sort(List<? extends RefEntity> entities) {
		entities.sort((e1, e2) -> Strings.compare(e1.name, e2.name));
	}

	String toPath(Category category) {
		return category != null
				? category.toPath()
				: "";
	}

	@FunctionalInterface
	interface CsvWriter {
		void writeTo(CSVPrinter csv) throws IOException;
	}

}
