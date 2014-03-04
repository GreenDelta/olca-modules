package org.openlca.simapro.csv.writer;

import static org.openlca.simapro.csv.writer.WriterUtils.comment;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.openlca.simapro.csv.model.SPCalculatedParameter;
import org.openlca.simapro.csv.model.SPInputParameter;
import org.openlca.simapro.csv.model.SPLiteratureReference;
import org.openlca.simapro.csv.model.SPParameter;
import org.openlca.simapro.csv.model.SPQuantity;
import org.openlca.simapro.csv.model.SPReferenceData;
import org.openlca.simapro.csv.model.SPSubstance;
import org.openlca.simapro.csv.model.SPSystemDescription;
import org.openlca.simapro.csv.model.SPUnit;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ParameterType;

class ReferenceData {

	private CSVWriter writer;
	private char csvSeperator;
	private SPReferenceData referenceData = null;
	private Queue<SPSubstance> substances = new LinkedList<>();

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
		SPSystemDescription systemDescription = referenceData
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
		for (SPLiteratureReference literatureReference : referenceData
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
					comment(literatureReference.getContent()));
			writer.writeln("End");
			writer.newLine();
		}
	}

	private void writeQuantities() throws IOException {
		if (!referenceData.getQuantities().isEmpty()) {
			writer.writeln("Quantities");
			for (SPQuantity quantity : referenceData.getQuantities().values())
				writer.writeln(quantity.getName() + csvSeperator
						+ (quantity.isDimensional() ? "Yes" : "No"));
			writer.newLine();
			writer.writeln("End");
			writer.newLine();
		}
	}

	private void writeUnits() throws IOException {
		if (!referenceData.getUnits().isEmpty()) {
			writer.writeln("Units");
			for (SPUnit unit : referenceData.getUnits().values()) {
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
			writer.writeln(type.getValue());
			Iterator<SPSubstance> itr = substances.iterator();
			while (itr.hasNext()) {
				SPSubstance substance = itr.next();
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
			for (SPInputParameter parameter : referenceData
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
			for (SPCalculatedParameter parameter : referenceData
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

	private boolean containsType(Object parameters, ParameterType type) {
		@SuppressWarnings("unchecked")
		Map<String, SPParameter> map = (Map<String, SPParameter>) parameters;
		for (SPParameter p : map.values())
			if (p.getType() == type)
				return true;
		return false;
	}

	private boolean containsType(ElementaryFlowType type) {
		for (SPSubstance substance : substances)
			if (substance.getFlowType() == type)
				return true;
		return false;
	}
}
