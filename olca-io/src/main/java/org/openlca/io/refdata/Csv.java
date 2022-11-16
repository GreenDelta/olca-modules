package org.openlca.io.refdata;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Csv {

	private Csv() {
	}

	static CSVFormat format() {
		return CSVFormat.Builder.create()
			.setDelimiter(',')
			.setTrim(true)
			.setIgnoreEmptyLines(true)
			.setQuote('"')
			.setIgnoreSurroundingSpaces(true)
			.build();
	}

	static String get(CSVRecord row, int i) {
		return row == null || i >= row.size()
			? null
			: row.get(i);
	}

	static Double getOptionalDouble(CSVRecord row, int i) {
		var s = get(row, i);
		if (Strings.nullOrEmpty(s))
			return null;
		try {
			return Double.parseDouble(s);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Csv.class);
			log.error("{} is not a number; default to null", s);
			return null;
		}
	}

	static double getDouble(CSVRecord row, int i) {
		if (row == null || i >= row.size())
			return 0;
		var s = get(row, i);
		if (s == null)
			return 0;
		try {
			return Double.parseDouble(s);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Csv.class);
			log.error("{} is not a number; default to 0.0", s);
			return 0;
		}
	}

	static int getInt(CSVRecord row, int i) {
		var s = get(row, i);
		if (s == null)
			return 0;
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Csv.class);
			log.error("{} is not a number; default to 0", s);
			return 0;
		}
	}
}
