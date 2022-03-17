package org.openlca.core.library.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

class Csv {

	// Number of columns that the respective types take in an CSV index.
	static final int FLOW_COLS = 5;
	static final int LOCATION_COLS = 5;

	private Csv() {
	}

	static String str(String val) {
		return val == null ? "" : val;
	}

	static String read(CSVRecord row, int pos) {
		return row.size() >= pos
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

}
