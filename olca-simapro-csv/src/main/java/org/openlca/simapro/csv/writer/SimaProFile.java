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

class SimaProFile {

	private CSVWriter writer;
	private char csvSeparator;
	private char decimalSeparator;

	public SimaProFile(CSVWriter writer) {
		this.writer = writer;
		this.csvSeparator = writer.getSeparator();
		this.decimalSeparator = writer.getDecimalSeparator();
	}

	void write(SPDataSet dataEntry) throws IOException {
		writer.writeln("Process");
		writer.newLine();
		if (dataEntry.getDocumentation() != null)
			new Documentation().write(dataEntry.getDocumentation(),
					dataEntry instanceof SPProcess, writer);
		writer.newLine();
		writeReferenceProducts(dataEntry);
		writer.newLine();
		writeExchanges(dataEntry);
		writeParameters(dataEntry);
		writer.writeln("End");
		writer.newLine();
	}

	private void writeReferenceProducts(SPDataSet dataEntry) throws IOException {
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
		writeProductFlows(dataEntry, "Avoided products",
				ProductFlowType.AVOIDED_PRODUCT);
		writeElemFlows(dataEntry, "Resources", ElementaryFlowType.RESOURCE);
		writeProductFlows(dataEntry, "Materials/fuels",
				ProductFlowType.MATERIAL_INPUT);
		writeProductFlows(dataEntry, "Electricity/heat",
				ProductFlowType.ELECTRICITY_INPUT);
		writeElemFlows(dataEntry, "Emissions to air",
				ElementaryFlowType.EMISSION_TO_AIR);
		writeElemFlows(dataEntry, "Emissions to water",
				ElementaryFlowType.EMISSION_TO_WATER);
		writeElemFlows(dataEntry, "Emissions to soil",
				ElementaryFlowType.EMISSION_TO_SOIL);
		writeElemFlows(dataEntry, "Final waste flows",
				ElementaryFlowType.FINAL_WASTE);
		writeElemFlows(dataEntry, "Non material emissions",
				ElementaryFlowType.NON_MATERIAL_EMISSIONS);
		writeElemFlows(dataEntry, "Social issues",
				ElementaryFlowType.SOCIAL_ISSUE);
		writeElemFlows(dataEntry, "Economic issues",
				ElementaryFlowType.ECONOMIC_ISSUE);
		writeProductFlows(dataEntry, "Waste to treatment",
				ProductFlowType.WASTE_TREATMENT);

	}

	private void writeParameters(SPDataSet dataEntry) throws IOException {
		writer.writeln("Input parameters");
		for (SPInputParameter parameter : dataEntry.getInputParameters())
			writer.writeln(WriterUtils.getInputParameterLine(parameter,
					csvSeparator, decimalSeparator));
		writer.newLine();
		writer.writeln("Calculated parameters");
		for (SPCalculatedParameter parameter : dataEntry
				.getCalculatedParameters())
			writer.writeln(WriterUtils.getCalculatedParameterLine(parameter,
					csvSeparator, decimalSeparator));
		writer.newLine();
	}

	private void writeProductFlows(SPDataSet dataEntry, String header,
			ProductFlowType type) throws IOException {
		writer.writeln(header);
		for (SPProductFlow product : dataEntry.getProductFlows(type))
			writer.writeln(getProductLine(product));
		writer.newLine();
	}

	private void writeElemFlows(SPDataSet dataEntry, String header,
			ElementaryFlowType type) throws IOException {
		writer.writeln(header);
		for (SPElementaryFlow flow : dataEntry.getElementaryFlows(type))
			writer.writeln(getElementaryFlowLine(flow));
		writer.newLine();
	}

	private String getProductLine(SPProductFlow product) {
		String line = product.getName()
				+ csvSeparator
				+ product.getUnit()
				+ csvSeparator
				+ number(product.getAmount())
				+ csvSeparator
				+ WriterUtils.getDistributionPart(product.getDistribution(),
						csvSeparator, decimalSeparator);
		if (product.getComment() != null)
			line += comment(product.getComment());
		return line;
	}

	private String getProductLine(SPProduct product, String subCategory) {
		String line = product.getName() + csvSeparator + product.getUnit()
				+ csvSeparator + number(product.getAmount()) + csvSeparator
				+ product.getAllocation() + csvSeparator;

		if (product.getWasteType() != null
				&& !product.getWasteType().equals("")) {
			line += product.getWasteType();
		} else {
			line += "not defined";
		}
		line += csvSeparator;
		if (subCategory != null) {
			line += subCategory;
		} else {
			line += "Others";
		}
		line += csvSeparator;

		return line;
	}

	private String getElementaryFlowLine(SPElementaryFlow flow) {
		String line = flow.getName() + csvSeparator;
		if (flow.getSubCompartment() != null) {
			line += flow.getSubCompartment().getValue();
		}
		line += csvSeparator
				+ flow.getUnit()
				+ csvSeparator
				+ number(flow.getAmount())
				+ csvSeparator
				+ WriterUtils.getDistributionPart(flow.getDistribution(),
						csvSeparator, decimalSeparator);
		if (flow.getComment() != null)
			line += comment(flow.getComment());
		return line;
	}

	private String getWasteSpecificationLine(
			SPWasteSpecification wasteSpecification, String subCategory) {
		String line = wasteSpecification.getName() + csvSeparator
				+ wasteSpecification.getUnit() + csvSeparator
				+ number(wasteSpecification.getAmount()) + csvSeparator;
		if (wasteSpecification.getWasteType() != null
				&& !wasteSpecification.getWasteType().equals("")) {
			line += wasteSpecification.getWasteType();
		} else {
			line += "All waste types";
		}
		line += csvSeparator + "Others";
		if (subCategory != null) {
			line += subCategory;
		}
		line += csvSeparator;
		if (wasteSpecification.getComment() != null)
			line += comment(wasteSpecification.getComment());
		line += csvSeparator;
		return line;
	}

	private String number(String val) {
		if (val == null)
			return "0";
		else
			return val.replace('.', decimalSeparator);
	}

}
