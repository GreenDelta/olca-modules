package org.openlca.simapro.csv.writer;

import static org.openlca.simapro.csv.writer.WriterUtils.comment;

import java.io.IOException;

import org.openlca.simapro.csv.model.CalculatedParameterRow;
import org.openlca.simapro.csv.model.SPDataSet;
import org.openlca.simapro.csv.model.InputParameterRow;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPWasteTreatment;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ProductType;
import org.openlca.simapro.csv.model.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.model.process.ProductExchangeRow;
import org.openlca.simapro.csv.model.process.ProductOutputRow;
import org.openlca.simapro.csv.model.process.WasteTreatmentRow;

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
			for (ProductOutputRow product : SPProcess.class.cast(dataEntry)
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
		writeProductFlows(dataEntry, ProductType.AVOIDED_PRODUCTS);
		writeElemFlows(dataEntry, ElementaryFlowType.RESOURCES);
		writeProductFlows(dataEntry, ProductType.MATERIAL_FUELS);
		writeProductFlows(dataEntry, ProductType.ELECTRICITY_HEAT);
		writeElemFlows(dataEntry, ElementaryFlowType.EMISSIONS_TO_AIR);
		writeElemFlows(dataEntry, ElementaryFlowType.EMISSIONS_TO_WATER);
		writeElemFlows(dataEntry, ElementaryFlowType.EMISSIONS_TO_SOIL);
		writeElemFlows(dataEntry, ElementaryFlowType.FINAL_WASTE_FLOWS);
		writeElemFlows(dataEntry, ElementaryFlowType.NON_MATERIAL_EMISSIONS);
		writeElemFlows(dataEntry, ElementaryFlowType.SOCIAL_ISSUES);
		writeElemFlows(dataEntry, ElementaryFlowType.ECONOMIC_ISSUES);
		writeProductFlows(dataEntry, ProductType.WASTE_TO_TREATMENT);
	}

	private void writeParameters(SPDataSet dataEntry) throws IOException {
		writer.writeln("Input parameters");
		for (InputParameterRow parameter : dataEntry.getInputParameters())
			writer.writeln(WriterUtils.getInputParameterLine(parameter,
					csvSeparator, decimalSeparator));
		writer.newLine();
		writer.writeln("Calculated parameters");
		for (CalculatedParameterRow parameter : dataEntry
				.getCalculatedParameters())
			writer.writeln(WriterUtils.getCalculatedParameterLine(parameter,
					csvSeparator, decimalSeparator));
		writer.newLine();
	}

	private void writeProductFlows(SPDataSet dataEntry, ProductType type)
			throws IOException {
		writer.writeln(type.getHeader());
		for (ProductExchangeRow product : dataEntry.getProductFlows(type))
			writer.writeln(getProductLine(product));
		writer.newLine();
	}

	private void writeElemFlows(SPDataSet dataEntry, ElementaryFlowType type)
			throws IOException {
		writer.writeln(type.getExchangeHeader());
		for (ElementaryExchangeRow flow : dataEntry.getElementaryFlows(type))
			writer.writeln(getElementaryFlowLine(flow));
		writer.newLine();
	}

	private String getProductLine(ProductExchangeRow product) {
		return product.toCsv(Character.toString(csvSeparator));
	}

	private String getProductLine(ProductOutputRow product, String subCategory) {
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

	private String getElementaryFlowLine(ElementaryExchangeRow flow) {
		return flow.toCsv(Character.toString(csvSeparator));
	}

	private String getWasteSpecificationLine(
			WasteTreatmentRow wasteSpecification, String subCategory) {
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
