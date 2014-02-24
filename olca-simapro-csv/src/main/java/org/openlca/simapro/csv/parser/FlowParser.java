package org.openlca.simapro.csv.parser;

import java.util.Map;

import org.openlca.simapro.csv.model.SPElementaryFlow;
import org.openlca.simapro.csv.model.SPProduct;
import org.openlca.simapro.csv.model.SPProductFlow;
import org.openlca.simapro.csv.model.SPSubstance;
import org.openlca.simapro.csv.model.SPWasteSpecification;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ProcessCategory;
import org.openlca.simapro.csv.model.enums.ProductFlowType;
import org.openlca.simapro.csv.model.enums.SubCompartment;
import org.openlca.simapro.csv.parser.exception.CSVParserException;

class FlowParser {

	private String csvSeperator;
	private Map<String, String[]> index;

	FlowParser(String csvSeperator, Map<String, String[]> index) {
		this.csvSeperator = csvSeperator;
		this.index = index;
	}

	SPElementaryFlow parseElementaryFlow(String line, ElementaryFlowType type)
			throws CSVParserException {

		line += csvSeperator + " ";
		String split[] = line.split(csvSeperator);
		if (split.length < 9)
			throw new CSVParserException("Error in " + type.getValue()
					+ " line: " + line);

		String name = split[0];
		String subCompartment = split[1];
		String unit = split[2];
		String formula = split[3];
		String distribution = split[4];
		String dValue1 = Utils.formatNumber(split[5]);
		String dValue2 = Utils.formatNumber(split[6]);
		String dValue3 = Utils.formatNumber(split[7]);
		String comment = split[8];

		for (int i = 9; i < (split.length - 1); i++) {
			comment += csvSeperator + split[i];
		}
		return new SPElementaryFlow(type,
				SubCompartment.forValue(subCompartment), name, unit, formula,
				comment, Utils.createDistibution(distribution, dValue1,
						dValue2, dValue3, comment));
	}

	SPProductFlow getProductFlow(String line, ProductFlowType type)
			throws CSVParserException {
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
		SPProductFlow flow = new SPProductFlow(type, name, unit, formula,
				comment, Utils.createDistibution(distribution, dValue1,
						dValue2, dValue3, comment));
		String[] categoryTree = index.get(name);
		if (categoryTree != null) {
			flow.setProcessCategory(ProcessCategory.forValue(categoryTree[0]));
			StringBuilder builder = new StringBuilder();
			for (int i = 1; i < categoryTree.length; i++)
				builder.append(categoryTree[i] + "\\");
			String category = builder.toString();
			if (category.endsWith("\\"))
				category = category.substring(0, category.length() - 1);
			flow.setReferenceCategory(category);
		}
		return flow;
	}

	SPProduct readReferenceProduct(String line) throws CSVParserException {
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
		return new SPProduct(name, unit, formula,
				Double.parseDouble(allocation), wasteType, comment, category);
	}

	SPWasteSpecification readWasteSpecification(String line)
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

	static SPSubstance parseSubstance(String line, String csvSeperator,
			ElementaryFlowType type) {
		line += csvSeperator + " ";
		String split[] = line.split(csvSeperator);
		String name = split[0];
		String referenceUnit = split[1];
		String cas = split[2];
		String comment = split[3];

		for (int i = 4; i < (split.length - 1); i++) {
			comment += csvSeperator + split[i];
		}

		SPSubstance substance = new SPSubstance(name, referenceUnit);
		substance.setReferenceUnit(referenceUnit);
		substance.setCASNumber(cas);
		substance.setComment(comment);
		substance.setFlowType(type);
		return substance;
	}

}
