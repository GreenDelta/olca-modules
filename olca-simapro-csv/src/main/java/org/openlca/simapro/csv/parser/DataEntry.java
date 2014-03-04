package org.openlca.simapro.csv.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.openlca.simapro.csv.model.SPDataSet;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPProcessDocumentation;
import org.openlca.simapro.csv.model.SPProduct;
import org.openlca.simapro.csv.model.SPReferenceData;
import org.openlca.simapro.csv.model.SPWasteScenario;
import org.openlca.simapro.csv.model.SPWasteTreatment;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ParameterType;
import org.openlca.simapro.csv.model.enums.ProcessCategory;
import org.openlca.simapro.csv.model.enums.ProductFlowType;
import org.openlca.simapro.csv.parser.exception.CSVParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DataEntry {

	private Logger log = LoggerFactory.getLogger(getClass());

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

	private void addElementaryFlows(SPDataSet entry, ElementaryFlowType type,
			Queue<String> lines) throws CSVParserException {
		while (!lines.isEmpty() && !lines.peek().equals(""))
			entry.getElementaryFlows().add(
					flowParser.parseElementaryFlow(lines.poll(), type));
	}

	private void addProductFlows(SPDataSet entry, ProductFlowType type,
			Queue<String> lines) throws CSVParserException {
		while (!lines.isEmpty() && !lines.peek().equals(""))
			entry.getProductFlows().add(
					flowParser.getProductFlow(lines.poll(), type));

	}

	SPDataSet parse(Queue<String> lines) throws Exception {
		SPDataSet entry = initDataSet(lines);
		while (!lines.isEmpty()) {
			switch (lines.poll()) {
			case "Avoided products":
				addProductFlows(entry, ProductFlowType.AVOIDED_PRODUCTS, lines);
				break;
			case "Resources":
				addElementaryFlows(entry, ElementaryFlowType.RESOURCES, lines);
				break;
			case "Materials/fuels":
				addProductFlows(entry, ProductFlowType.MATERIAL_FUELS, lines);
				break;
			case "Electricity/heat":
				addProductFlows(entry, ProductFlowType.ELECTRICITY_HEAT, lines);
				break;
			case "Emissions to air":
				addElementaryFlows(entry, ElementaryFlowType.EMISSIONS_TO_AIR,
						lines);
				break;
			case "Emissions to water":
				addElementaryFlows(entry,
						ElementaryFlowType.EMISSIONS_TO_WATER, lines);
				break;
			case "Emissions to soil":
				addElementaryFlows(entry, ElementaryFlowType.EMISSIONS_TO_SOIL,
						lines);
				break;
			case "Final waste flows":
				addElementaryFlows(entry, ElementaryFlowType.FINAL_WASTE_FLOWS,
						lines);
				break;
			case "Non material emissions":
				addElementaryFlows(entry,
						ElementaryFlowType.NON_MATERIAL_EMISSIONS, lines);
				break;
			case "Social issues":
				addElementaryFlows(entry, ElementaryFlowType.SOCIAL_ISSUES,
						lines);
				break;
			case "Economic issues":
				addElementaryFlows(entry, ElementaryFlowType.ECONOMIC_ISSUES,
						lines);
				break;
			case "Waste to treatment":
				addProductFlows(entry, ProductFlowType.WASTE_TO_TREATMENT,
						lines);
				break;
			case "Input parameters":
				while (!lines.isEmpty() && !lines.peek().equals("")) {
					entry.getInputParameters().add(
							Parameter.parseInputParameter(lines.poll(),
									csvSeperator, ParameterType.LOCAL));
				}
				break;
			case "Calculated parameters":
				while (!lines.isEmpty() && !lines.peek().equals("")) {
					entry.getCalculatedParameters().add(
							Parameter.parseCalculatedParameter(lines.poll(),
									csvSeperator, ParameterType.LOCAL));
				}
				break;
			default:
				break;
			}
		}
		return entry;
	}

	private SPDataSet initDataSet(Queue<String> lines) throws Exception {
		SPProcessDocumentation documentation = new DataEntryDocumentation(
				csvSeperator, referenceData).parse(lines);
		SPDataSet entry = null;
		ProcessCategory category = documentation.getCategory();
		if (category == ProcessCategory.WASTE_SCENARIO) {
			log.warn("waste scenarios are not fully implemented");
			entry = new SPWasteScenario();
		} else if (category == ProcessCategory.WASTE_TREATMENT) {
			if (!lines.isEmpty())
				lines.remove();
			entry = readWasteTreatment(lines.poll());
		} else {
			if (!lines.isEmpty())
				lines.remove();
			entry = readProcess(lines);
		}
		entry.setDocumentation(documentation);
		return entry;
	}
}
