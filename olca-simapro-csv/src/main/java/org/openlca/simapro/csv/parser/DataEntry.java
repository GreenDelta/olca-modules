package org.openlca.simapro.csv.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.openlca.simapro.csv.model.SPDataEntry;
import org.openlca.simapro.csv.model.SPDocumentation;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPProductFlow;
import org.openlca.simapro.csv.model.SPReferenceProduct;
import org.openlca.simapro.csv.model.SPWasteSpecification;
import org.openlca.simapro.csv.model.SPWasteTreatment;
import org.openlca.simapro.csv.model.types.ElementaryFlowType;
import org.openlca.simapro.csv.model.types.ParameterType;
import org.openlca.simapro.csv.model.types.ProcessCategory;
import org.openlca.simapro.csv.model.types.ProductFlowType;

class DataEntry {

	private String csvSeperator;

	public DataEntry(String csvSeperator) {
		this.csvSeperator = csvSeperator;
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
		return new SPWasteTreatment(readWasteSpecification(line), subCategory,
				null);
	}

	private SPWasteSpecification readWasteSpecification(String line)
			throws CSVParserException {
		line += csvSeperator + " ";
		String split[] = line.split(csvSeperator);

		if (split.length < 6)
			throw new CSVParserException("Error in waste specification line: "
					+ line);

		String name = split[0];
		String unit = split[1];
		String formula = split[2];
		String wasteType = split[3];
		String category = split[4];
		String comment = split[5];

		for (int i = 6; i < (split.length - 1); i++) {
			comment += csvSeperator + split[i];
		}

		return new SPWasteSpecification(name, unit, formula, wasteType,
				comment, category);
	}

	private SPProcess readProcess(Queue<String> lines)
			throws CSVParserException {
		SPProcess process = null;
		List<SPReferenceProduct> referenceProducts = new ArrayList<SPReferenceProduct>();
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
			referenceProducts.add(readReference(lines.poll()));
		}
		process = new SPProcess(referenceProducts.get(0), subCategory, null);
		for (int i = 1; i < referenceProducts.size(); i++)
			process.add(referenceProducts.get(i));
		return process;
	}

	private SPReferenceProduct readReference(String line)
			throws CSVParserException {
		line += csvSeperator + " ";
		String split[] = line.split(csvSeperator);
		if (split.length < 7)
			throw new CSVParserException("Error in product line: " + line);
		String name = split[0];
		String unit = split[1];
		String formula = split[2];
		String allocation = Utils.formatNumber(split[3]);
		String wasteType = split[4];
		String category = split[5];
		String comment = split[6];

		for (int i = 7; i < (split.length - 1); i++)
			comment += csvSeperator + split[i];

		return new SPReferenceProduct(name, unit, formula,
				Double.parseDouble(allocation), wasteType, comment, category);
	}

	private void addElementaryFlows(SPDataEntry entry, ElementaryFlowType type,
			Queue<String> lines) throws CSVParserException {
		while (!lines.isEmpty() && !lines.peek().equals(""))
			entry.add(Flows.parseElementaryFlow(lines.poll(), csvSeperator,
					type));
	}

	private void addProductFlows(SPDataEntry entry, ProductFlowType type,
			Queue<String> lines) throws CSVParserException {
		while (!lines.isEmpty() && !lines.peek().equals(""))
			entry.add(getProductFlow(lines.poll(), csvSeperator, type));

	}

	private SPProductFlow getProductFlow(String line, String csvSeperator,
			ProductFlowType type) throws CSVParserException {
		line += csvSeperator + " ";
		String split[] = line.split(csvSeperator);
		if (split.length < 8)
			throw new CSVParserException("Error in " + type.getValue()
					+ " line: " + line);
		String name = split[0];
		String unit = split[1];
		String formula = split[2];
		String distribution = split[3];
		String dValue1 = Utils.formatNumber(split[4]);
		String dValue2 = Utils.formatNumber(split[5]);
		String dValue3 = Utils.formatNumber(split[6]);
		String comment = split[7];

		for (int i = 8; i < (split.length - 1); i++)
			comment += csvSeperator + split[i];

		return new SPProductFlow(type, name, unit, formula, comment,
				Utils.createDistibution(distribution, dValue1, dValue2,
						dValue3, comment));
	}

	SPDataEntry parse(Queue<String> lines) throws CSVParserException {
		SPDataEntry entry = null;
		SPDocumentation documentation = new DataEntryDocumentation(csvSeperator)
				.parse(lines);

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
