package org.openlca.core.matrix.io.index;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * A utility class for serializing indices to CSV.
 */
class Csv {

	// Number of columns that the respective types take in an CSV index.
	static final int FLOW_COLS = 5;
	static final int PROCESS_COLS = 4;
	static final int LOCATION_COLS = 3;

	private Csv() {
	}

	static String str(String val) {
		return val == null ? "" : val;
	}

	static String read(CSVRecord row, int pos) {
		return pos >= row.size()
			? null
			: row.get(pos);
	}

	static int readInt(CSVRecord row, int pos) {
		var s = read(row, pos);
		return s != null
			? Integer.parseInt(s)
			: 0;
	}

	static boolean readBool(CSVRecord row, int pos) {
		var s = read(row, pos);
		return Boolean.parseBoolean(s);
	}

	static CSVFormat format() {
		return CSVFormat.RFC4180;
	}

	static boolean isCsv(File file) {
		return file != null && file.exists()
			&& file.getName().toLowerCase().endsWith(".csv");
	}

	static void eachRowSkipFirst(File file, Consumer<CSVRecord> fn) {
		try (var reader = new FileReader(file, StandardCharsets.UTF_8);
				 var parser = new CSVParser(reader, Csv.format())) {
			boolean isFirst = true;
			for (var record : parser) {
				if (isFirst) {
					isFirst = false;
					continue;
				}
				fn.accept(record);
			}
		} catch (IOException e) {
			throw new RuntimeException("failed to read CSV file: " + file, e);
		}
	}
}
