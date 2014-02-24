package org.openlca.simapro.csv.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.openlca.simapro.csv.model.SPDataSet;
import org.openlca.simapro.csv.model.SPLiteratureReference;
import org.openlca.simapro.csv.model.SPReferenceData;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ParameterType;
import org.openlca.simapro.csv.parser.exception.CSVMultipleLiteratureReferenceNameException;
import org.openlca.simapro.csv.parser.exception.CSVMultipleProcessNameException;
import org.openlca.simapro.csv.parser.exception.CSVParserException;

public class CSVParser {

	private String csvSeperator;
	// private String decimalSeparator; // TODO
	// private String dateSeparator; // TODO
	private String encoding = "windows-1252";
	private File file;
	private BufferedReader reader;
	private SPReferenceData referenceData;
	private Map<String, String[]> index;
	private FlowParser flowParser;

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

	public SPReferenceData getReferenceData() {
		return referenceData;
	}

	public Map<String, String[]> getIndex() {
		return index;
	}

	public void start() throws IOException, CSVParserException {
		index = createProductIndex();
		reader = createBufferedReader();
		flowParser = new FlowParser(csvSeperator, index);
		reader.close();
		reader = createBufferedReader();
		readHeader();
		referenceData = new SPReferenceData();
		readReferenceData();
		reader.close();
		reader = createBufferedReader();
	}

	public boolean hasNext() throws IOException {
		while ((currentLine = reader.readLine()) != null)
			if (currentLine.equals("Process")) {
				break;
			}
		return "Process".equals(currentLine);
	}

	public SPDataSet next() throws CSVParserException, IOException {
		if (!"Process".equals(currentLine))
			if (!hasNext())
				return null;
		return new DataEntry(csvSeperator, flowParser, referenceData)
				.parse(readNextPart());
	}

	public void close() throws IOException {
		reader.close();
	}

	/**
	 * @return key: product name / waste specification name
	 * 
	 *         value: String[] with a category tree
	 * 
	 * @throws IOException
	 * @throws CSVParserException
	 */
	private Map<String, String[]> createProductIndex() throws IOException,
			CSVParserException {
		Map<String, String[]> index = new HashMap<String, String[]>();
		Set<String> multipleNames = new HashSet<>();
		reader = createBufferedReader();
		readHeader();
		while (hasNext()) {
			Object[] entry = nextIndexEntry();
			if (entry == null)
				continue;
			String name = (String) entry[0];
			if (index.containsKey(name))
				multipleNames.add(name);
			index.put(name, (String[]) entry[1]);
		}
		if (!multipleNames.isEmpty()) {
			StringBuilder message = new StringBuilder();
			message.append("The following names occur more than once:");
			for (String name : multipleNames)
				message.append("\n" + name);
			throw new CSVMultipleProcessNameException(message.toString());
		}
		return index;
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

	private Object[] nextIndexEntry() throws CSVParserException, IOException {
		if (!"Process".equals(currentLine))
			if (!hasNext())
				return null;
		return IndexEntry.parse(readNextPart(), csvSeperator);
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
				SPLiteratureReference reference = LiteratureReference
						.parse(readNextPart());
				if (referenceData.add(reference.getName(), reference))
					throw new CSVMultipleLiteratureReferenceNameException(
							reference.getName());
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
			referenceData.add(FlowParser.parseSubstance(lines.poll(),
					csvSeperator, type));
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
