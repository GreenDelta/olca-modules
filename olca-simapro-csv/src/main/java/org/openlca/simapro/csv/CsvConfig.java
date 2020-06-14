package org.openlca.simapro.csv;

import java.io.File;
import java.util.Objects;

import org.openlca.simapro.csv.io.FileHeaderReader;
import org.slf4j.LoggerFactory;

public class CsvConfig {

	public char separator;
	public char delimiter;
	public String dateFormat;

	public static CsvConfig getDefault() {
		CsvConfig config = new CsvConfig();
		config.separator = ';';
		config.delimiter = '"';
		config.dateFormat = "dd.MM.yyyy";
		return config;
	}

	public static CsvConfig of(File file) {
		CsvConfig config = CsvConfig.getDefault();
		try {
			var reader = new FileHeaderReader(file);
			var header = reader.read();
			if (header.getShortDateFormat() != null)
				config.dateFormat = header.getShortDateFormat();
			if (Objects.equals("Semicolon", header.getCsvSeparator()))
				config.separator = ';';
			else if (Objects.equals("Comma", header.getCsvSeparator()))
				config.separator = ',';
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(CsvConfig.class);
			log.error("failed to read header CSV entries from " + file, e);
		}
		return config;
	}
}
