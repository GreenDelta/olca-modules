package org.openlca.core.io.maps;

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

class Csv {

	private Csv() {
	}

	static String getString(CSVRecord row, int i) {
		return row == null || i >= row.size()
				? null
				: row.get(i);
	}

	static double getDouble(CSVRecord row, int i) {
		if (row == null || i >= row.size())
			return 0;
		var s = getString(row, i);
		if (s == null)
			return 0;
		try {
			return Double.parseDouble(s);
		} catch (Exception e) {
			return 0;
		}
	}

	static void each(InputStream stream, Consumer<CSVRecord> fn) {
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

	static void write(OutputStream out, Stream<Object[]> rows) {
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

	static CSVFormat format() {
		return CSVFormat.Builder.create()
				.setDelimiter(';')
				.setTrim(true)
				.setIgnoreEmptyLines(true)
				.setQuote('"')
				.setIgnoreSurroundingSpaces(true)
				.build();
	}
}
