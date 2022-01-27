package org.openlca.core.library.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

class Csv {

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
		if (s == null)
			return 0;
		return Integer.parseInt(s);
	}

	static CSVFormat format() {
		return CSVFormat.RFC4180;
	}

}
