package org.openlca.simapro.csv.writer;

import org.openlca.simapro.csv.model.SPDataSet;
import org.openlca.simapro.csv.model.SPReferenceData;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CSVWriter implements Closeable {

	private SimaProFile simaProFile;
	private char separator = ';';
	private char decimalSeparator = '.';
	private final BufferedWriter buffer;

	public CSVWriter(File file) throws IOException {
		// TODO: set encoding to windows-1252
		FileWriter writer = new FileWriter(file);
		buffer = new BufferedWriter(writer);
		simaProFile = new SimaProFile(this);
	}

	public void setDecimalSeparator(char decimalSeparator) {
		this.decimalSeparator = decimalSeparator;
	}

	public char getDecimalSeparator() {
		return decimalSeparator;
	}

	public void setSeparator(char separator) {
		this.separator = separator;
	}

	public char getSeparator() {
		return separator;
	}

	void writeln(String line) throws IOException {
		buffer.write(line);
		buffer.newLine();
	}

	void writeEntry(String name, String value) throws IOException {
		writeln(name);
		writeln(value != null ? value : "");
		buffer.newLine();
	}

	void newLine() throws IOException {
		buffer.newLine();
	}

	@Override
	public void close() throws IOException {
		buffer.flush();
		buffer.close();
	}

	public void writeHeader(String project) throws IOException {
		writeln("{SimaPro 7.3}");
		writeln("{processes}");
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
		writeln("{Date: " + dateFormat.format(new Date()) + "}");
		writeln("{Time: " + timeFormat.format(new Date()) + "}");
		writeln("{Project: " + project + "}");
		writeln("{CSV Format version: 7.0.0}");
		writeln("{CSV separator: " + getSeparatorName() + "}");
		writeln("{Decimal separator: " + decimalSeparator + "}");
		writeln("{Date separator: /}");
		writeln("{Short date format: M/d/yyyy}");
	}

	public void write(SPDataSet dataEntry) throws IOException {
		simaProFile.write(dataEntry);
	}

	public void write(SPReferenceData referenceData) throws IOException {
		ReferenceData writer = new ReferenceData(this);
		writer.write(referenceData);
	}

	private String getSeparatorName() {
		switch (separator) {
			case ';':
				return "Semicolon";
			case ',':
				return "Comma";
			case '\t':
				return "Tab";
			default:
				return "Unknown";
		}

	}
}
