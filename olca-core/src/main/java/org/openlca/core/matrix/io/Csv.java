package org.openlca.core.matrix.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

import org.openlca.core.matrix.format.ByteMatrixReader;
import org.openlca.core.matrix.format.MatrixReader;

public final class Csv {

	private DecimalFormat numberFormat;
	private String delimiter = ",";
	private Charset charset = StandardCharsets.UTF_8;

	public static Csv defaultConfig() {
		return new Csv();
	}

	public String quote(String s) {
		if (s == null)
			return "\"\"";
		return s.contains("\"")
			? "\"" + s.replace("\"", "\"\"") + "\""
			: "\"" + s + "\"";
	}

	public Csv withDelimiter(String delimiter) {
		if (delimiter != null) {
			this.delimiter = delimiter;
		}
		return this;
	}

	public Csv withEncoding(Charset charset) {
		if (charset != null) {
			this.charset = charset;
		}
		return this;
	}

	public Csv withDecimalSeparator(char separator) {
		if (separator == '.') {
			numberFormat = null;
			return this;
		}
		numberFormat = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		numberFormat.setMaximumFractionDigits(1000);
		numberFormat.setGroupingUsed(false);
		var symbols = numberFormat.getDecimalFormatSymbols();
		symbols.setDecimalSeparator(separator);
		numberFormat.setDecimalFormatSymbols(symbols);
		return this;
	}

	public void write(double[] vector, File file) {
		if (vector == null || file == null)
			return;
		writer(file, w -> {
			for (double v : vector) {
				writeln(w, format(v));
			}
		});
	}

	public void write(MatrixReader matrix, File file) {
		if (matrix == null || file == null)
			return;
		var buffer = new String[matrix.columns()];
		writer(file, w -> {
			for (int row = 0; row < matrix.rows(); row++) {
				for (int col = 0; col < matrix.columns(); col++) {
					buffer[col] = format(matrix.get(row, col));
				}
				writeln(w, line(buffer));
			}
		});
	}

	private String format(double v) {
		if (v == 0)
			return "0";
		return numberFormat == null
			? Double.toString(v)
			: numberFormat.format(v);
	}

	public void write(ByteMatrixReader matrix, File file) {
		if (matrix == null || file == null)
			return;
		var buffer = new String[matrix.columns()];
		writer(file, w -> {
			for (int row = 0; row < matrix.rows(); row++) {
				for (int col = 0; col < matrix.columns(); col++) {
					buffer[col] = Integer.toString(matrix.get(row, col));
				}
				writeln(w, line(buffer));
			}
		});
	}

	void writeln(BufferedWriter writer, String line) {
		try {
			writer.write(line);
			writer.newLine();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	void writer(File file, Consumer<BufferedWriter> fn) {
		try (var stream = new FileOutputStream(file);
				 var writer = new OutputStreamWriter(stream, charset);
				 var buffer = new BufferedWriter(writer)) {
			fn.accept(buffer);
			buffer.flush();
		} catch (IOException e) {
			throw new RuntimeException("Failed to write file: " + file.getName(), e);
		}
	}

	String line(String[] entries) {
		if (entries == null)
			return "";
		var b = new StringBuilder();
		for (int i = 0; i < entries.length; i++) {
			if (i != 0) {
				b.append(delimiter);
			}
			var e = entries[i];
			if(e != null) {
				b.append(e);
			}
		}
		return b.toString();
	}
}
