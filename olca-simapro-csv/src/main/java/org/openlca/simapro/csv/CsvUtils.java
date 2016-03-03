package org.openlca.simapro.csv;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * Utility functions for reading and writing SimaPro data sets.
 */
public class CsvUtils {

	private CsvUtils() {
	}

	public static Joiner getJoiner(CsvConfig config) {
		return Joiner.on(config.getSeparator()).useForNull("");
	}

	public static String getPedigreeUncertainty(String comment) {
		if (comment == null)
			return null;
		String pattern = "\\(\\s*([1-5]|na)\\s*,\\s*([1-5]|na)\\s*,\\s*([1-5]|na)"
				+ "\\s*,\\s*([1-5]|na)\\s*,\\s*([1-5]|na)\\s*,\\s*([1-5]|na)\\s*\\)";
		Matcher matcher = Pattern.compile(pattern).matcher(comment);
		boolean match = matcher.find();
		if (!match)
			return null;
		else
			return matcher.group();
	}

	public static String[] split(String line, CsvConfig config) {
		if (line == null)
			return new String[0];
		if (config == null || config.getSeparator() == 0)
			return new String[] { line };
		return _split(line, config);
	}

	private static String[] _split(String line, CsvConfig config) {
		List<String> lines = new ArrayList<>();
		String current = "";
		boolean inString = false;
		boolean lastWasDelimiter = false;
		for (char next : line.toCharArray()) {
			if (next == config.getDelimiter()) {
				inString = !inString;
				if (!lastWasDelimiter) {
					lastWasDelimiter = true;
					continue;
				}
			}
			lastWasDelimiter = false;
			if (next == config.getSeparator() && !inString) {
				lines.add(current);
				current = "";
				continue;
			}
			current += next;
		}
		lines.add(current);
		return lines.toArray(new String[lines.size()]);
	}

	public static String get(String[] columns, int col) {
		if (columns == null || col < 0)
			return null;
		if (col >= columns.length)
			return null;
		return strip(columns[col]);
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

	public static String readMultilines(String multilineString) {
		if (multilineString == null)
			return null;
		return multilineString.replace(((char) 127), '\n');
	}

	public static String writeMultilines(String multilineString) {
		if (multilineString == null)
			return null;
		return multilineString.replace('\n', ((char) 127));
	}

	/**
	 * Removes quotes and white spaces at the beginning and end of the given
	 * value.
	 */
	public static String strip(String value) {
		if (value == null)
			return null;
		String v = value.trim();
		if (v.length() < 2)
			return v;
		if (v.startsWith("\"") && v.endsWith("\"")) {
			v = v.substring(1, v.length() - 1);
		}
		return v;
	}

}
