package org.openlca.simapro.csv.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.openlca.simapro.csv.model.FileHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileHeaderReader {

	private Logger log = LoggerFactory.getLogger(getClass());

	private File file;
	private String charset;
	private Reader reader;

	public FileHeaderReader(File file) {
		this(file, "windows-1252");
	}

	public FileHeaderReader(File file, String charset) {
		this.file = file;
		this.charset = charset;
	}

	public FileHeaderReader(Reader reader) {
		this.reader = reader;
	}

	public FileHeader read() throws Exception {
		log.trace("read header entries");
		if (reader == null)
			openFile();
		FileHeader header = new FileHeader();
		try (BufferedReader buffer = new BufferedReader(reader)) {
			String line = null;
			int row = 0;
			while ((line = buffer.readLine()) != null) {
				line = line.trim();
				if (!line.startsWith("{") && !line.endsWith("}")) {
					log.trace("stop reading header at line {}", row);
					break;
				}
				readLine(row, line, header);
				row++;
			}
		}
		return header;
	}

	private void readLine(int row, String rawLine, FileHeader header) {
		String line = rawLine.substring(1, rawLine.length() - 1);
		log.trace("header: {}: {}", row, line);
		if (row == 0)
			header.setSimaProVersion(line);
		if (row == 1)
			header.setContentType(line);
		if (line.startsWith("Date: "))
			header.setDate(triml("Date: ", line));
		if (line.startsWith("Time: "))
			header.setTime(triml("Time: ", line));
		if (line.startsWith("Project: "))
			header.setProject(triml("Project: ", line));
		if (line.startsWith("CSV Format version: "))
			header.setFormatVersion(triml("CSV Format version: ", line));
		if (line.startsWith("CSV separator: "))
			header.setCsvSeparator(triml("CSV separator: ", line));
		if (line.startsWith("Decimal separator: "))
			header.setDecimalSeparator(triml("Decimal separator: ", line));
		if (line.startsWith("Date separator: "))
			header.setDateSeparator(triml("Date separator: ", line));
		if (line.startsWith("Short date format: "))
			header.setShortDateFormat(triml("Short date format: ", line));
	}

	private void openFile() throws Exception {
		log.trace("open file {}; encoding = {}", file, charset);
		FileInputStream stream = new FileInputStream(file);
		reader = new InputStreamReader(stream, charset);
	}

	private String triml(String prefix, String entry) {
		return entry.substring(prefix.length()).trim();
	}

}
