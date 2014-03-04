package org.openlca.simapro.csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility functions for reading and writing SimaPro data sets.
 */
public class CsvUtils {

	private CsvUtils() {
	}

	public static String[] split(String line, String separator) {
		if (line == null)
			return new String[0];
		if (separator == null)
			return new String[] { line };
		return line.split(separator);
	}

	public static String get(String[] columns, int col) {
		if (columns == null || col < 0)
			return null;
		if (col >= columns.length)
			return null;
		return columns[col];
	}

	public static void set(String val, String[] columns, int col) {
		if (columns == null || col < 0)
			return;
		if (col >= columns.length)
			return;
		columns[col] = val;
	}

	public static Double getDouble(String[] columns, int col) {
		String s = get(columns, col);
		if (s == null)
			return null;
		s = formatNumber(s);
		try {
			return Double.parseDouble(s);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(CsvUtils.class);
			log.warn("invalid number format: {}", s);
			return null;
		}
	}

	public static String formatNumber(String value) {
		if (value == null)
			return null;
		return value.replace(",", ".");
	}

}
