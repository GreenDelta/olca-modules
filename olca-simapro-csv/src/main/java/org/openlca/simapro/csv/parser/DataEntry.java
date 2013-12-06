package org.openlca.simapro.csv.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.openlca.simapro.csv.model.SPDataEntry;
import org.openlca.simapro.csv.model.SPDocumentation;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPProduct;
import org.openlca.simapro.csv.model.SPReferenceData;
import org.openlca.simapro.csv.model.SPWasteTreatment;
import org.openlca.simapro.csv.model.types.ElementaryFlowType;
import org.openlca.simapro.csv.model.types.ParameterType;
import org.openlca.simapro.csv.model.types.ProcessCategory;
import org.openlca.simapro.csv.model.types.ProductFlowType;
import org.openlca.simapro.csv.parser.exception.CSVParserException;

class DataEntry {

	private String csvSeperator;
	private FlowParser flowParser;
	SPReferenceData referenceData;

	DataEntry(String csvSeperator, FlowParser flowParser,
			SPReferenceData referenceData) {
		this.csvSeperator = csvSeperator;
		this.flowParser = flowParser;
		this.referenceData = referenceData;
	}

	private SPWasteTreatment readWasteTreatment(String line)
			throws CSVParserException {
		if (line == null || line.equals(""))
			throw new CSVParserException("Waste specification line is null.");

		int semCounter = 0;
		String subCategory = line;
		while (semCounter < 4 && subCategory.contains(csvSeperator)) {
			subCategory = subCategory.substring(subCategory
					.indexOf(csvSeperator) + 1);
			semCounter++;
		}
		if (subCategory.contains(csvSeperator)) {
			subCategory = subCategory.substring(0,
					subCategory.indexOf(csvSeperator));
		}
		return new SPWasteTreatment(flowParser.readWasteSpecification(line),
				subCategory, null);
	}

	private SPProcess readProcess(Queue<String> lines)
			throws CSVParserException {
		SPProcess process = null;
		List<SPProduct> referenceProducts = new ArrayList<SPProduct>();
		String subCategory = null;

		if (lines.isEmpty() || lines.peek().equals(""))
			throw new CSVParserException("Product line is null.");

		while (!lines.isEmpty() && !lines.peek().equals("")) {
			if (subCategory == null) {
				int semCounter = 0;
				subCategory = lines.peek();
				while (semCounter < 5 && subCategory.contains(csvSeperator)) {
					subCategory = subCategory.substring(subCategory
							.indexOf(csvSeperator) + 1);
					semCounter++;
				}
				if (subCategory.contains(csvSeperator)) {
					subCategory = subCategory.substring(0,
							subCategory.indexOf(csvSeperator));
				}
			}
			referenceProducts
					.add(flowParser.readReferenceProduct(lines.poll()));
		}
		process = new SPProcess(referenceProducts.get(0), subCategory, null);
		for (int i = 1; i < referenceProducts.size(); i++)
			process.add(referenceProducts.get(i));
		return process;
	}

	private void addElementaryFlows(SPDataEntry entry, ElementaryFlowType type,
			Queue<String> lines) throws CSVParserException {
		while (!lines.isEmpty() && !lines.peek().equals(""))
			entry.add(flowParser.parseElementaryFlow(lines.poll(), type));
	}

	private void addProductFlows(SPDataEntry entry, ProductFlowType type,
			Queue<String> lines) throws CSVParserException {
		while (!lines.isEmpty() && !lines.peek().equals(""))
			entry.add(flowParser.getProductFlow(lines.poll(), type));

	}

	SPDataEntry parse(Queue<String> lines) throws CSVParserException {
		SPDataEntry entry = null;
		SPDocumentation documentation = new DataEntryDocumentation(
				csvSeperator, referenceData).parse(lines);

		// TODO implement parse waste scenario
		if (documentation.getCategory() == ProcessCategory.WASTE_SCENARIO)
			throw new CSVParserException("Waste scenarios not implemented.");

		if (documentation.getCategory() == ProcessCategory.WASTE_TREATMENT) {
			if (!lines.isEmpty())
				lines.remove();
			entry = readWasteTreatment(lines.poll());
		} else {
			if (!lines.isEmpty())
				lines.remove();
			entry = readProcess(lines);
		}
		entry.setDocumentation(documentation);

		while (!lines.isEmpty()) {
			switch (lines.poll()) {
			case "Avoided products":
				addProductFlows(entry, ProductFlowType.AVOIDED_PRODUCT, lines);
				break;
			case "Resources":
				addElementaryFlows(entry, ElementaryFlowType.RESOURCE, lines);
				break;
			case "Materials/fuels":
				addProductFlows(entry, ProductFlowType.MATERIAL_INPUT, lines);
				break;
			case "Electricity/heat":
				addProductFlows(entry, ProductFlowType.ELECTRICITY_INPUT, lines);
				break;
			case "Emissions to air":
				addElementaryFlows(entry, ElementaryFlowType.EMISSION_TO_AIR,
						lines);
				break;
			case "Emissions to water":
				addElementaryFlows(entry, ElementaryFlowType.EMISSION_TO_WATER,
						lines);
				break;
			case "Emissions to soil":
				addElementaryFlows(entry, ElementaryFlowType.EMISSION_TO_SOIL,
						lines);
				break;
			case "Final waste flows":
				addElementaryFlows(entry, ElementaryFlowType.FINAL_WASTE, lines);
				break;
			case "Non material emissions":
				addElementaryFlows(entry,
						ElementaryFlowType.NON_MATERIAL_EMISSIONS, lines);
				break;
			case "Social issues":
				addElementaryFlows(entry, ElementaryFlowType.SOCIAL_ISSUE,
						lines);
				break;
			case "Economic issues":
				addElementaryFlows(entry, ElementaryFlowType.ECONOMIC_ISSUE,
						lines);
				break;
			case "Waste to treatment":
				addProductFlows(entry, ProductFlowType.WASTE_TREATMENT, lines);
				break;
			case "Input parameters":
				while (!lines.isEmpty() && !lines.peek().equals("")) {
					entry.add(Parameter.parseInputParameter(lines.poll(),
							csvSeperator, ParameterType.LOCAL));
				}
				break;
			case "Calculated parameters":
				while (!lines.isEmpty() && !lines.peek().equals("")) {
					entry.add(Parameter.parseCalculatedParameter(lines.poll(),
							csvSeperator, ParameterType.LOCAL));
				}
				break;
			default:
				// TODO if line not "" maybe throw exception?
				break;
			}
		}
		return entry;
	}

}
