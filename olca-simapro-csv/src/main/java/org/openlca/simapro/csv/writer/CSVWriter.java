package org.openlca.simapro.csv.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openlca.simapro.csv.CSVSeperator;
import org.openlca.simapro.csv.model.SPDataEntry;
import org.openlca.simapro.csv.model.SPReferenceData;

public class CSVWriter extends BufferedWriter {

	private Process process;
	CSVSeperator csvSeperator;
	char decimalSeperator;

	public CSVWriter(FileWriter writer, CSVSeperator csvSeperator,
			char decimalSeperator) {
		super(writer);
		this.csvSeperator = csvSeperator;
		this.decimalSeperator = decimalSeperator;
		process = new Process(this);
	}

	void writeln(String line) throws IOException {
		write(line);
		newLine();
	}

	void writeEntry(String name, String value) throws IOException {
		writeln(name);
		writeln(value != null ? value : "");
		newLine();
	}

	public void writeHeader(String project) throws IOException {
		// TODO
		writeln("{SimaPro 7.3}");
		writeln("{processes}");
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
		writeln("{Date: " + dateFormat.format(new Date()) + "}");
		writeln("{Time: " + timeFormat.format(new Date()) + "}");
		writeln("{Project: " + project + "}");
		writeln("{CSV Format version: 7.0.0}");
		writeln("{CSV separator: " + csvSeperator.getName() + "}");
		writeln("{Decimal separator: .}");
		writeln("{Date separator: /}");
		writeln("{Short date format: M/d/yyyy}");
	}

	public void write(SPDataEntry dataEntry) throws IOException {
		process.write(dataEntry);
	}

	public void write(SPReferenceData referenceData) throws IOException {
		ReferenceData writer = new ReferenceData(this);
		writer.write(referenceData);
	}
}
