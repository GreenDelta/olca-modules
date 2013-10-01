package org.openlca.simapro.csv.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Queue;

import org.openlca.simapro.csv.model.SPDataEntry;
import org.openlca.simapro.csv.model.SPReferenceData;
import org.openlca.simapro.csv.model.types.ElementaryFlowType;
import org.openlca.simapro.csv.model.types.ParameterType;

public class CSVParser {

	private String csvSeperator;
	private String decimalSeparator; // TODO
	private String dateSeparator; // TODO
	private String encoding = "windows-1252";
	private BufferedReader reader;
	private SPReferenceData referenceData;
	private File file;

	public CSVParser(File file) {
		this.file = file;
	}

	/**
	 * default: windows-1252
	 * 
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public SPReferenceData start() throws IOException, CSVParserException {
		reader = createBufferedReader();
		readHeader();
		referenceData = new SPReferenceData();
		readReferenceData();
		reader.close();
		reader = createBufferedReader();
		return referenceData;
	}

	private BufferedReader createBufferedReader()
			throws UnsupportedEncodingException, FileNotFoundException {
		BufferedReader reader;
		if (encoding == null)
			reader = new BufferedReader(new FileReader(file));
		else
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), encoding));
		return reader;
	}

	private String currentLine;

	public boolean hasNext() throws IOException {
		while ((currentLine = reader.readLine()) != null)
			if (currentLine.equals("Process")) {
				break;
			}
		return "Process".equals(currentLine);
	}

	public SPDataEntry next() throws CSVParserException, IOException {
		if (!"Process".equals(currentLine))
			if (!hasNext())
				return null;
		return new DataEntry(csvSeperator).parse(readNextPart());
	}

	private void readReferenceData() throws IOException, CSVParserException {
		String line;
		while ((line = reader.readLine()) != null) {
			switch (line) {
			case "System description":
				referenceData.setSystemDescription(SystemDescription.parse(
						readNextPart(), csvSeperator));
				break;
			case "Literature reference":
				referenceData.add(LiteratureReference.parse(readNextPart()));
				break;
			case "Quantities":
				parseAndNotify(ObjectType.QUANTITIES);
				break;
			case "Units":
				parseAndNotify(ObjectType.UNITS);
				break;
			case "Raw materials":
				parseAndNotify(ElementaryFlowType.RESOURCE);
				break;
			case "Airborne emissions":
				parseAndNotify(ElementaryFlowType.EMISSION_TO_AIR);
				break;
			case "Waterborne emissions":
				parseAndNotify(ElementaryFlowType.EMISSION_TO_WATER);
				break;
			case "Final waste flows":
				parseAndNotify(ElementaryFlowType.FINAL_WASTE);
				break;
			case "Emissions to soil":
				parseAndNotify(ElementaryFlowType.EMISSION_TO_SOIL);
				break;
			case "Social issues":
				parseAndNotify(ElementaryFlowType.SOCIAL_ISSUE);
				break;
			case "Economic issues":
				parseAndNotify(ElementaryFlowType.ECONOMIC_ISSUE);
				break;
			case "Non material emissions":
				parseAndNotify(ElementaryFlowType.NON_MATERIAL_EMISSIONS);
				break;
			case "Database Input parameters":
				parseAndNotify(ObjectType.INPARAM_DB);
				break;
			case "Database Calculated parameters":
				parseAndNotify(ObjectType.CALCPARAM_DB);
				break;
			case "Project Input parameters":
				parseAndNotify(ObjectType.INPARAM_PJ);
				break;
			case "Project Calculated parameters":
				parseAndNotify(ObjectType.CALCPARAM_PJ);
				break;
			}
		}
	}

	private void parseAndNotify(ElementaryFlowType type)
			throws CSVParserException, IOException {
		Queue<String> lines = readNextPart();
		while (!lines.isEmpty()
				&& !(lines.peek().equals("") || lines.peek().equals("End"))) {
			referenceData.add(Flows.parseSubstance(lines.poll(), csvSeperator,
					type));
		}
	}

	private void parseAndNotify(ObjectType type) throws CSVParserException,
			IOException {
		Queue<String> lines = readNextPart();
		while (!lines.isEmpty()
				&& !(lines.peek().equals("") || lines.peek().equals("End"))) {
			switch (type) {
			case CALCPARAM_DB:
				referenceData.add(Parameter.parseCalculatedParameter(
						lines.poll(), csvSeperator, ParameterType.DATABASE));
				break;
			case CALCPARAM_PJ:
				referenceData.add(Parameter.parseCalculatedParameter(
						lines.poll(), csvSeperator, ParameterType.PROJECT));
				break;
			case INPARAM_PJ:
				referenceData.add(Parameter.parseInputParameter(lines.poll(),
						csvSeperator, ParameterType.PROJECT));
				break;
			case INPARAM_DB:
				referenceData.add(Parameter.parseInputParameter(lines.poll(),
						csvSeperator, ParameterType.DATABASE));
				break;
			case QUANTITIES:
				referenceData.add(Quantity.parse(lines.poll(), csvSeperator));
				break;
			case UNITS:
				referenceData.add(Unit.parse(lines.poll(), csvSeperator));
				break;
			}
		}
	}

	private Queue<String> readNextPart() throws IOException {
		Queue<String> lines = new LinkedList<String>();
		String line;
		while ((line = reader.readLine()) != null && !line.equals("End")) {
			lines.add(line.replace(((char) 127) + "", "\n"));
		}
		return lines;
	}

	private void readHeader() throws IOException {
		String line;
		while ((line = reader.readLine()) != null
				&& !line.contains("{Short date format:")) {
			if (line.startsWith("{CSV separator:")) {
				line = line.toLowerCase();
				if (line.contains("semicolon"))
					csvSeperator = ";";
				else if (line.contains("tab"))
					csvSeperator = "\t";
				else if (line.contains("comma"))
					csvSeperator = ",";
			}
			// TODO other options
		}
	}

	private enum ObjectType {
		INPARAM_DB, CALCPARAM_DB, INPARAM_PJ, CALCPARAM_PJ, QUANTITIES, UNITS;
	}
}
