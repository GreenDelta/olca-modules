package org.openlca.io.maps;

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
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public static CSVFormat format() {
		return CSVFormat.Builder.create()
			.setDelimiter(';')
			.setTrim(true)
			.setIgnoreEmptyLines(true)
			.setQuote('"')
			.setIgnoreSurroundingSpaces(true)
			.build();
	}

	public static String getString(CSVRecord row, int i) {
		return row == null || i >= row.size()
			? null
			: row.get(i);
	}

	public static Double getOptionalDouble(CSVRecord row, int i) {
		var s = getString(row, i);
		if (Strings.nullOrEmpty(s))
			return null;
		try {
			return Double.parseDouble(s);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Maps.class);
			log.error("{} is not a number; default to null", s);
			return null;
		}
	}

	public static double getDouble(CSVRecord row, int i) {
		if (row == null || i >= row.size())
			return 0;
		var s = getString(row, i);
		if (s == null)
			return 0;
		try {
			return Double.parseDouble(s);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Maps.class);
			log.error("{} is not a number; default to 0.0", s);
			return 0;
		}
	}

	public static int getInt(CSVRecord row, int i) {
		var s = getString(row, i);
		if (s == null)
			return 0;
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Maps.class);
			log.error("{} is not a number; default to 0", s);
			return 0;
		}
	}

	/**
	 * Iterates over each row in the given mapping file.
	 */
	public static void each(File file, Consumer<CSVRecord> fn) {
		if (file == null || fn == null)
			return;
		try (var stream = new FileInputStream(file)) {
			each(stream, fn);
		} catch (Exception e) {
			throw new RuntimeException("failed to read mapping file " + file, e);
		}
	}

	/**
	 * Iterates over each row in the given mapping file.
	 */
	public static void each(InputStream stream, Consumer<CSVRecord> fn) {
		if (stream == null || fn == null)
			return;
		try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
				 var parser = new CSVParser(reader, format())) {
			for (var row : parser) {
				if (row.size() == 0)
					continue;
				fn.accept(row);
			}
		} catch (IOException e) {
			throw new RuntimeException("failed to read CSV stream", e);
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
		try (var writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
				 var printer = new CSVPrinter(writer, format())) {
			rows.forEach(row -> {
				if (row == null)
					return;
				try {
					printer.printRecord(row);
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
