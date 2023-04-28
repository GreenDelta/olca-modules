package org.openlca.io.simapro.csv.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

class CsvWriter implements AutoCloseable {

	private final BufferedWriter buffer;

	private CsvWriter(BufferedWriter buffer) {
		this.buffer = buffer;
	}

	static CsvWriter on(File file) {
		try {
			var stream = new FileOutputStream(file);
			var writer = new OutputStreamWriter(stream, "windows-1252");
			var buffer = new BufferedWriter(writer);
			return new CsvWriter(buffer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		try {
			buffer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	CsvWriter ln(Object... objects) {
		try {
			if (objects == null || objects.length == 0) {
				buffer.write("\r\n");
				return this;
			}

			var strings = new String[objects.length];
			for (int i = 0; i < objects.length; i++) {
				var obj = objects[i];
				if (obj == null) {
					strings[i] = "";
					continue;
				}
				if (obj instanceof String) {
					var s = ((String) obj)
							.replace(';', ',')
							.replace("\r", "")
							.replace('\n', '\u007F');
					if (s.contains("\"")) {
						s = "\"" + s + "\"";
					}
					strings[i] = s;
					continue;
				}
				if (obj instanceof Boolean) {
					strings[i] = ((Boolean) obj)
							? "Yes"
							: "No";
				}
				strings[i] = obj.toString();
			}

			var row = strings.length == 1
					? strings[0]
					: String.join(";", strings);

			buffer.write(row);
			buffer.write("\r\n");
			return this;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	void endSection() {
		ln().ln("End").ln();
	}

	void writerHeader(String project) {
		ln("{SimaPro 8.5.0.0}");
		ln("{processes}");
		var date = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
		ln("{Date: " + date + "}");
		var time = new SimpleDateFormat("HH:mm:ss").format(new Date());
		ln("{Time: " + time + "}");
		ln("{Project: " + project + "}");
		ln("{CSV Format version: 8.0.5}");
		ln("{CSV separator: Semicolon}");
		ln("{Decimal separator: .}");
		ln("{Date separator: .}");
		ln("{Short date format: dd.MM.yyyy}");
		ln();
	}
}
