package org.openlca.io.maps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

/**
 * A helper class for using import / export maps. We store the mappings in CSV
 * files of a defined format (see the mapping specification for the details:
 * REF_DATA.md). A default file for each mapping should be always directly
 * attached as jar-resource in the package org.openlca.io.maps. Additionally, a
 * mapping file can be stored in the database (so that it can be updated without
 * updating the io-package).
 */
public class Maps {

	private Maps() {
	}

	public static String getString(List<?> values, int i) {
		if (values == null || i >= values.size())
			return null;
		var val = values.get(i);
		return val == null
				? null
				: val.toString();
	}

	public static Double getOptionalDouble(List<?> values, int i) {
		if (values == null || i >= values.size())
			return null;
		Object val = values.get(i);
		if (val instanceof Number)
			return ((Number) val).doubleValue();
		if (val instanceof String) {
			if (Strings.nullOrEmpty((String) val))
				return null;
			try {
				return Double.parseDouble((String) val);
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(Maps.class);
				log.error("{} is not a number; default to null", val);
				return null;
			}
		}
		return null;
	}

	public static double getDouble(List<?> values, int i) {
		if (values == null || i >= values.size())
			return 0;
		Object val = values.get(i);
		if (val instanceof Number)
			return ((Number) val).doubleValue();
		if (val instanceof String) {
			try {
				return Double.parseDouble((String) val);
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(Maps.class);
				log.error("{} is not a number; default to 0.0", val);
				return 0;
			}
		}
		return 0;
	}

	public static int getInt(List<?> values, int i) {
		if (values == null || i >= values.size())
			return 0;
		Object val = values.get(i);
		if (val instanceof Number)
			return ((Number) val).intValue();
		if (val instanceof String) {
			try {
				return Integer.parseInt((String) val);
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(Maps.class);
				log.error("{} is not a number; default to 0", val);
				return 0;
			}
		}
		return 0;
	}

	/**
	 * Iterates over each row in the given mapping file.
	 */
	public static void each(File file, Consumer<List<String>> fn) {
		if (file == null || fn == null)
			return;
		try (var stream = new FileInputStream(file)){
		 	each(stream, fn);
		} catch (IOException e) {
			throw new RuntimeException("failed to read mapping file " + file, e);
		}
	}

	/**
	 * Iterates over each row in the given mapping file.
	 */
	public static void each(InputStream stream, Consumer<List<String>> fn) {
		if (stream == null || fn == null)
			return;
		var prefs = new CsvPreference.Builder('"', ';', "\n").build();
		try (var bom = new BOMInputStream(stream, false, ByteOrderMark.UTF_8);
			 var r = new InputStreamReader(bom, StandardCharsets.UTF_8);
			 var buf = new BufferedReader(r);
			 var reader = new CsvListReader(buf, prefs)) {
			List<String> row;
			while ((row = reader.read()) != null) {
				if (row.isEmpty())
					continue;
				fn.accept(row);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Converts the given rows into the CSV mapping format and writes this to
	 * the given file.
	 */
	public static void write(File file, Stream<Object[]> rows) {
		if (file == null || rows == null)
			return;
		try (var out = new FileOutputStream(file)) {
			write(out, rows);
		} catch (IOException e) {
			throw new RuntimeException("failed to write mapping file " + file, e);
		}
	}

	/**
	 * Converts the given rows into the CSV mapping format and writes this to
	 * a byte array.
	 */
	public static byte[] write(Stream<Object[]> rows) {
		if (rows == null)
			return new byte[0];
		var bout = new ByteArrayOutputStream();
		write(bout, rows);
		return bout.toByteArray();
	}

	/**
	 * Converts the given rows into the CSV mapping format and writes this to
	 * the given output stream.
	 */
	public static void write(OutputStream out, Stream<Object[]> rows) {
		if (out == null || rows == null)
			return;
		var prefs = new CsvPreference.Builder('"', ';', "\n").build();
		try (var w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
			 var buf = new BufferedWriter(w);
			 var writer = new CsvListWriter(buf, prefs)) {
			rows.forEach(row -> {
				if (row == null)
					return;
				try {
					writer.write(row);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
