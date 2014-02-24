package org.openlca.simapro.csv.writer;

import static org.openlca.simapro.csv.writer.WriterUtils.comment;

import java.io.IOException;

import org.openlca.simapro.csv.model.SPCalculatedParameter;
import org.openlca.simapro.csv.model.SPDataSet;
import org.openlca.simapro.csv.model.SPElementaryFlow;
import org.openlca.simapro.csv.model.SPInputParameter;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPProduct;
import org.openlca.simapro.csv.model.SPProductFlow;
import org.openlca.simapro.csv.model.SPWasteSpecification;
import org.openlca.simapro.csv.model.SPWasteTreatment;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ProductFlowType;

class Process {
	private CSVWriter writer;
	private char csvSeperator;
	private char decimalSeperator;

	public Process(CSVWriter writer) {
		this.writer = writer;
		this.csvSeperator = writer.csvSeperator.getSeperator();
		this.decimalSeperator = writer.decimalSeperator;
	}

	void write(SPDataSet dataEntry) throws IOException {
		writer.writeln("Process");
		writer.newLine();
		if (dataEntry.getDocumentation() != null)
			new Documentation().write(dataEntry.getDocumentation(),
					dataEntry instanceof SPProcess ? true : false, writer);
		writer.newLine();
		writeReferenceProducts(dataEntry);
		writer.newLine();
		writeExchanges(dataEntry);
		writer.writeln("End");
		writer.newLine();
	}

	private void writeReferenceProducts(SPDataSet dataEntry)
			throws IOException {
		if (dataEntry instanceof SPProcess) {
			writer.writeln("Products");
			String subCategory = SPProcess.class.cast(dataEntry)
					.getSubCategory();
			writer.writeln(getProductLine(SPProcess.class.cast(dataEntry)
					.getReferenceProduct(), subCategory));
			for (SPProduct product : SPProcess.class.cast(dataEntry)
					.getByProducts())
				writer.writeln(getProductLine(product, subCategory));
		} else if (dataEntry instanceof SPWasteTreatment) {
			writer.writeln("Waste treatment");
			writer.writeln(getWasteSpecificationLine(SPWasteTreatment.class
					.cast(dataEntry).getWasteSpecification(),
					SPWasteTreatment.class.cast(dataEntry).getSubCategory()));
		}
	}

	private void writeExchanges(SPDataSet dataEntry) throws IOException {
		writer.writeln("Avoided products");
		for (SPProductFlow product : dataEntry
				.getProductFlows(ProductFlowType.AVOIDED_PRODUCT))
			writer.writeln(getProductLine(product));
		writer.newLine();
		writer.writeln("Resources");
		for (SPElementaryFlow flow : dataEntry
				.getElementaryFlows(ElementaryFlowType.RESOURCE))
			writer.writeln(getElementaryFlowLine(flow));
		writer.newLine();
		writer.writeln("Materials/fuels");
		for (SPProductFlow product : dataEntry
				.getProductFlows(ProductFlowType.MATERIAL_INPUT))
			writer.writeln(getProductLine(product));
		writer.newLine();
		writer.writeln("Electricity/heat");
		for (SPProductFlow product : dataEntry
				.getProductFlows(ProductFlowType.ELECTRICITY_INPUT))
			writer.writeln(getProductLine(product));
		writer.newLine();
		writer.writeln("Emissions to air");
		for (SPElementaryFlow flow : dataEntry
				.getElementaryFlows(ElementaryFlowType.EMISSION_TO_AIR))
			writer.writeln(getElementaryFlowLine(flow));
		writer.newLine();
		writer.writeln("Emissions to water");
		for (SPElementaryFlow flow : dataEntry
				.getElementaryFlows(ElementaryFlowType.EMISSION_TO_WATER))
			writer.writeln(getElementaryFlowLine(flow));
		writer.newLine();
		writer.writeln("Emissions to soil");
		for (SPElementaryFlow flow : dataEntry
				.getElementaryFlows(ElementaryFlowType.EMISSION_TO_SOIL))
			writer.writeln(getElementaryFlowLine(flow));
		writer.newLine();
		writer.writeln("Final waste flows");
		for (SPElementaryFlow flow : dataEntry
				.getElementaryFlows(ElementaryFlowType.FINAL_WASTE))
			writer.writeln(getElementaryFlowLine(flow));
		writer.newLine();
		writer.writeln("Non material emissions");
		for (SPElementaryFlow flow : dataEntry
				.getElementaryFlows(ElementaryFlowType.NON_MATERIAL_EMISSIONS))
			writer.writeln(getElementaryFlowLine(flow));
		writer.newLine();
		writer.writeln("Social issues");
		for (SPElementaryFlow flow : dataEntry
				.getElementaryFlows(ElementaryFlowType.SOCIAL_ISSUE))
			writer.writeln(getElementaryFlowLine(flow));
		writer.newLine();
		writer.writeln("Economic issues");
		for (SPElementaryFlow flow : dataEntry
				.getElementaryFlows(ElementaryFlowType.ECONOMIC_ISSUE))
			writer.writeln(getElementaryFlowLine(flow));
		writer.newLine();
		writer.writeln("Waste to treatment");
		for (SPProductFlow product : dataEntry
				.getProductFlows(ProductFlowType.WASTE_TREATMENT))
			writer.writeln(getProductLine(product));
		writer.newLine();
		writer.writeln("Input parameters");
		for (SPInputParameter parameter : dataEntry.getInputParameters())
			writer.writeln(WriterUtils.getInputParameterLine(parameter,
					csvSeperator, decimalSeperator));
		writer.newLine();
		writer.writeln("Calculated parameters");
		for (SPCalculatedParameter parameter : dataEntry
				.getCalculatedParameters())
			writer.writeln(WriterUtils.getCalculatedParameterLine(parameter,
					csvSeperator, decimalSeperator));
		writer.newLine();
	}

	private String getProductLine(SPProductFlow product) {
		String line = product.getName()
				+ csvSeperator
				+ product.getUnit()
				+ csvSeperator
				+ product.getAmount().replace('.', decimalSeperator)
				+ csvSeperator
				+ WriterUtils.getDistributionPart(product.getDistribution(),
						csvSeperator, decimalSeperator);
		if (product.getComment() != null)
			line += comment(product.getComment());
		return line;
	}

	private String getProductLine(SPProduct product, String subCategory) {
		String line = product.getName() + csvSeperator + product.getUnit()
				+ csvSeperator
				+ product.getAmount().replace('.', decimalSeperator)
				+ csvSeperator + product.getAllocation() + csvSeperator;

		if (product.getWasteType() != null
				&& !product.getWasteType().equals("")) {
			line += product.getWasteType();
		} else {
			line += "not defined";
		}
		line += csvSeperator;
		if (subCategory != null) {
			line += subCategory;
		} else {
			line += "Others";
		}
		line += csvSeperator;

		return line;
	}

	private String getElementaryFlowLine(SPElementaryFlow flow) {
		String line = flow.getName() + csvSeperator;
		if (flow.getSubCompartment() != null) {
			line += flow.getSubCompartment().getValue();
		}
		line += csvSeperator
				+ flow.getUnit()
				+ csvSeperator
				+ flow.getAmount().replace('.', decimalSeperator)
				+ csvSeperator
				+ WriterUtils.getDistributionPart(flow.getDistribution(),
						csvSeperator, decimalSeperator);
		if (flow.getComment() != null)
			line += comment(flow.getComment());
		return line;
	}

	private String getWasteSpecificationLine(
			SPWasteSpecification wasteSpecification, String subCategory) {
		String line = wasteSpecification.getName() + csvSeperator
				+ wasteSpecification.getUnit() + csvSeperator
				+ wasteSpecification.getAmount().replace('.', decimalSeperator)
				+ csvSeperator;
		if (wasteSpecification.getWasteType() != null
				&& !wasteSpecification.getWasteType().equals("")) {
			line += wasteSpecification.getWasteType();
		} else {
			line += "All waste types";
		}
		line += csvSeperator + "Others";
		if (subCategory != null) {
			line += subCategory;
		}
		line += csvSeperator;
		if (wasteSpecification.getComment() != null)
			line += comment(wasteSpecification.getComment());
		line += csvSeperator;
		return line;
	}

}
