package org.openlca.simapro.csv.writer;

import static org.openlca.simapro.csv.writer.WriterUtils.comment;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.openlca.simapro.csv.model.CalculatedParameterRow;
import org.openlca.simapro.csv.model.InputParameterRow;
import org.openlca.simapro.csv.model.SPReferenceData;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ParameterType;
import org.openlca.simapro.csv.model.refdata.ElementaryFlowRow;
import org.openlca.simapro.csv.model.refdata.LiteratureReferenceBlock;
import org.openlca.simapro.csv.model.refdata.Quantity;
import org.openlca.simapro.csv.model.refdata.SystemDescriptionBlock;
import org.openlca.simapro.csv.model.refdata.UnitRow;

class ReferenceData {

	private CSVWriter writer;
	private char csvSeperator;
	private SPReferenceData referenceData = null;
	private Queue<ElementaryFlowRow> substances = new LinkedList<>();

	public ReferenceData(CSVWriter writer) {
		this.writer = writer;
		csvSeperator = writer.getSeparator();
	}

	void write(SPReferenceData referenceData) throws IOException {
		this.referenceData = referenceData;
		substances.addAll(referenceData.getSubstances().values());
		if (referenceData != null) {
			writeSystemDescription();
			writeLiteratureReference();
			writeQuantities();
			writeUnits();
			for (ElementaryFlowType type : ElementaryFlowType.values())
				writeSubstances(type);
			writeParameters(ParameterType.DATABASE);
			writeParameters(ParameterType.PROJECT);
		}
	}

	private void writeSystemDescription() throws IOException {
		SystemDescriptionBlock systemDescription = referenceData
				.getSystemDescription();
		if (systemDescription != null) {
			writer.writeln("System description");
			writer.newLine();
			writer.writeEntry("Name", systemDescription.getName());
			writer.writeEntry(
					"Category",
					systemDescription.getCategory() != null ? systemDescription
							.getCategory() : "Others");
			writer.writeEntry("Description",
					comment(systemDescription.getDescription()));
			writer.writeEntry("Sub-systems",
					comment(systemDescription.getSubSystems()));
			writer.writeEntry("Cut-off rules",
					comment(systemDescription.getCutOffRules()));
			writer.writeEntry("Energy model",
					comment(systemDescription.getEnergyModel()));
			writer.writeEntry("Transport model",
					comment(systemDescription.getTransportModel()));
			writer.writeEntry("Waste model",
					comment(systemDescription.getWasteModel()));
			writer.writeEntry("Other assumptions",
					comment(systemDescription.getOtherAssumptions()));
			writer.writeEntry("Other information",
					comment(systemDescription.getOtherInformation()));
			writer.writeEntry("Allocation rules",
					comment(systemDescription.getAllocationRules()));
			writer.writeln("End");
			writer.newLine();
		}
	}

	private void writeLiteratureReference() throws IOException {
		for (LiteratureReferenceBlock literatureReference : referenceData
				.getLiteratureReferences().values()) {
			writer.writeln("Literature reference");
			writer.newLine();
			writer.writeEntry("Name", literatureReference.getName());
			writer.newLine();
			writer.writeEntry(
					"Category",
					literatureReference.getCategory() != null ? literatureReference
							.getCategory() : "Others");
			writer.newLine();
			writer.writeEntry("Description",
					comment(literatureReference.getDescription()));
			writer.writeln("End");
			writer.newLine();
		}
	}

	private void writeQuantities() throws IOException {
		if (!referenceData.getQuantities().isEmpty()) {
			writer.writeln("Quantities");
			for (Quantity quantity : referenceData.getQuantities().values())
				writer.writeln(quantity.getName() + csvSeperator
						+ (quantity.isWithDimension() ? "Yes" : "No"));
			writer.newLine();
			writer.writeln("End");
			writer.newLine();
		}
	}

	private void writeUnits() throws IOException {
		if (!referenceData.getUnits().isEmpty()) {
			writer.writeln("Units");
			for (UnitRow unit : referenceData.getUnits().values()) {
				StringBuilder builder = new StringBuilder();
				builder.append(unit.getName());
				builder.append(csvSeperator);
				builder.append(unit.getQuantity());
				builder.append(csvSeperator);
				builder.append(unit.getConversionFactor());
				builder.append(csvSeperator);
				builder.append(unit.getReferenceUnit());
				writer.writeln(builder.toString());
			}
			writer.newLine();
			writer.writeln("End");
			writer.newLine();
		}
	}

	private void writeSubstances(ElementaryFlowType type) throws IOException {
		if (containsType(type)) {
			writer.writeln(type.getReferenceHeader());
			Iterator<ElementaryFlowRow> itr = substances.iterator();
			while (itr.hasNext()) {
				ElementaryFlowRow substance = itr.next();
				if (substance.getFlowType() == type) {
					if (substance.getCASNumber() == null)
						substance.setCASNumber("");
					if (substance.getComment() == null)
						substance.setComment("");
					StringBuilder builder = new StringBuilder();
					builder.append(substance.getName());
					builder.append(csvSeperator);
					builder.append(substance.getReferenceUnit());
					builder.append(csvSeperator);
					builder.append(substance.getCASNumber());
					builder.append(csvSeperator);
					builder.append(comment(substance.getComment()));
					writer.writeln(builder.toString());
					itr.remove();
				}
			}
			writer.newLine();
			writer.writeln("End");
			writer.newLine();
		}
	}

	private void writeParameters(ParameterType parameterType)
			throws IOException {
		boolean input = containsType(referenceData.getInputParameters(),
				parameterType);
		boolean calc = containsType(referenceData.getCalculatedParameters(),
				parameterType);
		String name = "";
		if (parameterType == ParameterType.DATABASE)
			name = "Database";
		else if (parameterType == ParameterType.PROJECT)
			name = "Project";

		if (input) {
			writer.writeln(name + " Input parameters");
			for (InputParameterRow parameter : referenceData
					.getInputParameters().values())
				if (parameter.getType() == parameterType)
					writer.writeln(WriterUtils.getInputParameterLine(parameter,
							csvSeperator, writer.getDecimalSeparator()));
			writer.newLine();
			writer.writeln("End");
			writer.newLine();
		}
		if (calc) {
			writer.writeln(name + " Calculated parameters");
			for (CalculatedParameterRow parameter : referenceData
					.getCalculatedParameters().values())
				if (parameter.getType() == parameterType)
					writer.writeln(WriterUtils.getCalculatedParameterLine(
							parameter, csvSeperator,
							writer.getDecimalSeparator()));
			writer.newLine();
			writer.writeln("End");
			writer.newLine();
		}
	}

	private boolean containsType(ElementaryFlowType type) {
		for (ElementaryFlowRow substance : substances)
			if (substance.getFlowType() == type)
				return true;
		return false;
	}
}
