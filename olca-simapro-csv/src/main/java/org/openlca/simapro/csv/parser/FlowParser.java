package org.openlca.simapro.csv.parser;

import java.util.Map;

import org.openlca.simapro.csv.model.IDistribution;
import org.openlca.simapro.csv.model.SPElementaryExchange;
import org.openlca.simapro.csv.model.SPProduct;
import org.openlca.simapro.csv.model.SPProductFlow;
import org.openlca.simapro.csv.model.SPSubstance;
import org.openlca.simapro.csv.model.SPWasteSpecification;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ProcessCategory;
import org.openlca.simapro.csv.model.enums.ProductFlowType;
import org.openlca.simapro.csv.parser.exception.CSVParserException;

class FlowParser {

	private String csvSeperator;
	private Map<String, String[]> index;

	FlowParser(String csvSeperator, Map<String, String[]> index) {
		this.csvSeperator = csvSeperator;
		this.index = index;
	}

	SPElementaryExchange parseElementaryFlow(String line, ElementaryFlowType type)
			throws CSVParserException {
		line += csvSeperator + " ";
		String split[] = line.split(csvSeperator);
		if (split.length < 9)
			throw new CSVParserException("Error in " + type.getExchangeHeader()
					+ " line: " + line);

		SPElementaryExchange flow = new SPElementaryExchange();
		flow.setName(split[0]);
		flow.setSubCompartment(split[1]);
		flow.setUnit(split[2]);
		flow.setAmount(split[3]);
		IDistribution d = readDistribution(split, 4);
		flow.setDistribution(d);
		return flow;
	}

	private IDistribution readDistribution(String[] entries, int start) {
		String type = entries[start];
		String value1 = Utils.formatNumber(entries[start + 1]);
		String value2 = Utils.formatNumber(entries[start + 2]);
		String value3 = Utils.formatNumber(entries[start + 3]);
		String comment = entries[start + 4];
		for (int i = start + 5; i < (entries.length - 1); i++)
			comment += csvSeperator + entries[i];
		return Utils.createDistibution(type, value1, value2, value3, comment);
	}

	SPProductFlow getProductFlow(String line, ProductFlowType type)
			throws CSVParserException {
		line += csvSeperator + " ";
		String split[] = line.split(csvSeperator);
		if (split.length < 8)
			throw new CSVParserException("Error in " + type.getHeader()
					+ " line: " + line);
		SPProductFlow flow = new SPProductFlow();
		flow.setName(split[0]);
		flow.setUnit(split[1]);
		flow.setAmount(split[2]);
		IDistribution d = readDistribution(split, 3);
		flow.setDistribution(d);
		setFlowCategory(flow);
		return flow;
	}

	private void setFlowCategory(SPProductFlow flow) {
		String[] categoryTree = index.get(flow.getName());
		if (categoryTree == null)
			return;
		flow.setProcessCategory(ProcessCategory.forValue(categoryTree[0]));
		StringBuilder builder = new StringBuilder();
		for (int i = 1; i < categoryTree.length; i++)
			builder.append(categoryTree[i] + "\\");
		String category = builder.toString();
		if (category.endsWith("\\"))
			category = category.substring(0, category.length() - 1);
		flow.setReferenceCategory(category);
	}

	SPProduct readReferenceProduct(String line) throws CSVParserException {
		line += csvSeperator + " ";
		String split[] = line.split(csvSeperator);
		if (split.length < 7)
			throw new CSVParserException("Error in product line: " + line);
		SPProduct product = new SPProduct();
		product.setName(split[0]);
		product.setUnit(split[1]);
		product.setAmount(split[2]);
		product.setAllocation(Double.parseDouble(Utils.formatNumber(split[3])));
		product.setWasteType(split[4]);
		product.setCategory(split[5]);
		String comment = split[6];
		for (int i = 7; i < (split.length - 1); i++)
			comment += csvSeperator + split[i];
		product.setComment(comment);
		return product;
	}

	SPWasteSpecification readWasteSpecification(String line)
			throws CSVParserException {
		line += csvSeperator + " ";
		String split[] = line.split(csvSeperator);
		if (split.length < 6)
			throw new CSVParserException("Error in waste specification line: "
					+ line);
		SPWasteSpecification waste = new SPWasteSpecification();
		waste.setName(split[0]);
		waste.setUnit(split[1]);
		waste.setAmount(split[2]);
		waste.setWasteType(split[3]);
		waste.setCategory(split[4]);
		String comment = split[5];
		for (int i = 6; i < (split.length - 1); i++) {
			comment += csvSeperator + split[i];
		}
		waste.setComment(comment);
		return waste;
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
