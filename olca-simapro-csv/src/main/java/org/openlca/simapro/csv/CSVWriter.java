package org.openlca.simapro.csv;

import static org.openlca.simapro.csv.WriterUtils.comment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openlca.simapro.csv.model.IDistribution;
import org.openlca.simapro.csv.model.SPCalculatedParameter;
import org.openlca.simapro.csv.model.SPDataSet;
import org.openlca.simapro.csv.model.SPDocumentation;
import org.openlca.simapro.csv.model.SPElementaryFlow;
import org.openlca.simapro.csv.model.SPInputParameter;
import org.openlca.simapro.csv.model.SPLiteratureReference;
import org.openlca.simapro.csv.model.SPLiteratureReferenceEntry;
import org.openlca.simapro.csv.model.SPLogNormalDistribution;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPProductFlow;
import org.openlca.simapro.csv.model.SPQuantity;
import org.openlca.simapro.csv.model.SPReferenceProduct;
import org.openlca.simapro.csv.model.SPSystemDescription;
import org.openlca.simapro.csv.model.SPSystemDescriptionEntry;
import org.openlca.simapro.csv.model.SPUnit;
import org.openlca.simapro.csv.model.SPWasteSpecification;
import org.openlca.simapro.csv.model.SPWasteToTreatmentFlow;
import org.openlca.simapro.csv.model.SPWasteTreatment;
import org.openlca.simapro.csv.model.types.BoundaryWithNature;
import org.openlca.simapro.csv.model.types.CutOffRule;
import org.openlca.simapro.csv.model.types.DistributionParameterType;
import org.openlca.simapro.csv.model.types.DistributionType;
import org.openlca.simapro.csv.model.types.ElementaryFlowType;
import org.openlca.simapro.csv.model.types.Geography;
import org.openlca.simapro.csv.model.types.ProcessAllocation;
import org.openlca.simapro.csv.model.types.ProcessCategory;
import org.openlca.simapro.csv.model.types.ProductFlowType;
import org.openlca.simapro.csv.model.types.Representativeness;
import org.openlca.simapro.csv.model.types.Status;
import org.openlca.simapro.csv.model.types.Substitution;
import org.openlca.simapro.csv.model.types.SystemBoundary;
import org.openlca.simapro.csv.model.types.Technology;
import org.openlca.simapro.csv.model.types.TimePeriod;
import org.openlca.simapro.csv.model.types.WasteTreatmentAllocation;

/**
 * The CSV writer creates a new SimaPro CSV file and writes the given data set
 * into it
 */
public class CSVWriter {

	private char separator = ',';

	/**
	 * The buffered writer for writing the CSV file
	 */
	private BufferedWriter writer = null;

	public CSVWriter() {
	}

	public CSVWriter(char separator) {
		this.separator = separator;
	}

	/**
	 * Searches the data set for system descriptions
	 * 
	 * @param dataSet
	 *            The data set to search in
	 * @return All system descriptions the data set contains
	 */
	private SPSystemDescription[] collectSystemDescriptions(SPDataSet dataSet) {
		List<SPSystemDescription> systemDescriptions = new ArrayList<SPSystemDescription>();
		List<String> descriptionNames = new ArrayList<String>();
		for (SPProcess process : dataSet.getProcesses()) {
			SPDocumentation doc = process.getDocumentation();
			addSystemDescriptions(doc, systemDescriptions, descriptionNames);
		}
		for (SPWasteTreatment wt : dataSet.getWasteTreatments()) {
			SPDocumentation doc = wt.getDocumentation();
			addSystemDescriptions(doc, systemDescriptions, descriptionNames);
		}
		return systemDescriptions
				.toArray(new SPSystemDescription[systemDescriptions.size()]);
	}

	private void addSystemDescriptions(SPDocumentation doc,
			List<SPSystemDescription> descriptions, List<String> names) {
		if (doc == null)
			return;
		SPSystemDescriptionEntry entry = doc.getSystemDescriptionEntry();
		if (entry == null)
			return;
		SPSystemDescription sd = entry.getSystemDescription();
		if (!names.contains(sd.getName())) {
			names.add(sd.getName());
			descriptions.add(sd);
		}
	}

	/**
	 * Searches the data set for literature references
	 * 
	 * @param dataSet
	 *            The data set to search in
	 * @return All literature references the data set contains
	 */
	private SPLiteratureReference[] collectLiteratureReferences(
			SPDataSet dataSet) {
		List<SPLiteratureReference> literatureReferences = new ArrayList<SPLiteratureReference>();
		List<String> referenceNames = new ArrayList<String>();
		for (SPProcess process : dataSet.getProcesses()) {
			for (SPLiteratureReferenceEntry entry : process.getDocumentation()
					.getLiteratureReferencesEntries()) {
				SPLiteratureReference lr = entry.getLiteratureReference();
				if (!referenceNames.contains(lr.getName())) {
					referenceNames.add(lr.getName());
					literatureReferences.add(lr);
				}
			}
		}
		for (SPWasteTreatment wt : dataSet.getWasteTreatments()) {
			for (SPLiteratureReferenceEntry entry : wt.getDocumentation()
					.getLiteratureReferencesEntries()) {
				SPLiteratureReference lr = entry.getLiteratureReference();
				if (!referenceNames.contains(lr.getName())) {
					referenceNames.add(lr.getName());
					literatureReferences.add(lr);
				}
			}
		}
		return literatureReferences
				.toArray(new SPLiteratureReference[literatureReferences.size()]);
	}

	/**
	 * Writes a literature reference into the CSV file
	 * 
	 * @param literatureReference
	 *            The reference to write
	 * @throws IOException
	 */
	private void writeLiteratureReference(
			SPLiteratureReference literatureReference) throws IOException {
		writeln("Literature reference");
		writer.newLine();
		writeEntry("Name", literatureReference.getName());
		writer.newLine();
		writeEntry(
				"Category",
				literatureReference.getCategory() != null ? literatureReference
						.getCategory() : "Others");
		writer.newLine();
		writeEntry("Description", literatureReference.getContent());
		writer.newLine();
		writeln("End");
		writer.newLine();
	}

	/**
	 * Creates the distribution part of a flow line
	 * 
	 * @param distribution
	 *            The distribution of the flow
	 * @return The distribution part of a flow line in a CSV file
	 */
	private String getDistributionPart(IDistribution distribution) {
		String line = "";
		if (distribution == null) {
			line = "Undefined;0;0;0;";
		} else {
			DistributionType type = distribution.getType();
			if (type == null) {
				type = DistributionType.UNDEFINED;
			}
			line = type.getValue().replace('.', separator) + ";";
			switch (distribution.getType()) {
			case LOG_NORMAL:
				SPLogNormalDistribution logNormalDistribution = (SPLogNormalDistribution) distribution;

				line += distribution
						.getDistributionParameter(DistributionParameterType.SQUARED_STANDARD_DEVIATION)
						+ ";0;0;"
						+ logNormalDistribution.getPedigreeMatrix()
								.getPedigreeCommentString();
				break;
			case NORMAL:
				line += distribution
						.getDistributionParameter(DistributionParameterType.DOUBLED_STANDARD_DEVIATION)
						+ ";0;0;";
				break;
			case TRIANGLE:
				line += "0;"
						+ distribution
								.getDistributionParameter(DistributionParameterType.MINIMUM)
						+ ";"
						+ distribution
								.getDistributionParameter(DistributionParameterType.MAXIMUM)
						+ ";";
				break;
			case UNIFORM:
				line += "0;"
						+ distribution
								.getDistributionParameter(DistributionParameterType.MINIMUM)
						+ ";"
						+ distribution
								.getDistributionParameter(DistributionParameterType.MAXIMUM)
						+ ";";
				break;
			case UNDEFINED:
				line += "0;0;0;";
				break;
			}
		}
		return line;
	}

	/**
	 * Creates an elementary flow line
	 * 
	 * @param flow
	 *            The flow to be written
	 * @return The elementary flow line
	 */
	private String getElementaryFlowLine(SPElementaryFlow flow) {
		String line = flow.getSubstance().getName() + ";";
		if (flow.getSubCompartment() != null) {
			line += flow.getSubCompartment().getValue();
		}
		line += ";" + flow.getUnit().getName() + ";"
				+ flow.getAmount().replace('.', separator) + ";"
				+ getDistributionPart(flow.getDistribution())
				+ comment(flow.getComment());
		return line;
	}

	/**
	 * Creates the product input line for a process
	 * 
	 * @param product
	 *            The product flow to write
	 * @return A product input entry for a process
	 */
	private String getProductLine(SPProductFlow product) {
		String line = product.getName() + ";" + product.getUnit().getName()
				+ ";" + product.getAmount().replace('.', separator) + ";"
				+ getDistributionPart(product.getDistribution());
		if (product.getComment() != null)
			line += comment(product.getComment());
		return line;
	}

	/**
	 * Creates the flow line for a waste to treatment flow
	 * 
	 * @param flow
	 * @return line
	 */
	private String getWasteToTreatmentLine(SPWasteToTreatmentFlow flow) {
		String line = flow.getName() + ";" + flow.getUnit().getName() + ";"
				+ flow.getAmount() + ";"
				+ flow.getDistributionType().getValue() + ";"
				+ flow.getStandardDeviation() + ";" + flow.getMin() + ";"
				+ flow.getMax() + ";";

		return line;
	}

	/**
	 * Creates the product line for a process
	 * 
	 * @param product
	 *            The reference product flow of the process
	 * @return The product line for a process in a CSV file
	 */
	private String getProductLine(SPReferenceProduct product, String subCategory) {
		String line = product.getName() + ";" + product.getUnit().getName()
				+ ";" + product.getAmount().replace('.', separator) + ";"
				+ product.getAllocation() + ";";
		if (product.getWasteType() != null
				&& !product.getWasteType().equals("")) {
			line += product.getWasteType();
		} else {
			line += "not defined";
		}
		line += ";";
		if (subCategory != null) {
			line += subCategory;
		} else {
			line += "Others";
		}
		line += ";";
		if (product.getComment() != null)
			line += comment(product.getComment());
		line += ";";
		return line;
	}

	/**
	 * Creates a waste specification line
	 * 
	 * @param wasteSpecification
	 *            The waste specification
	 * @param subCategory
	 *            The sub category of the waste treatment holding the waste
	 *            specification
	 * @return The waste treatment line for a CSV file
	 */
	private String getWasteSpecificationLine(
			SPWasteSpecification wasteSpecification, String subCategory) {
		String line = wasteSpecification.getName() + ";"
				+ wasteSpecification.getUnit().getName() + ";"
				+ wasteSpecification.getAmount().replace('.', separator) + ";";
		if (wasteSpecification.getWasteType() != null
				&& !wasteSpecification.getWasteType().equals("")) {
			line += wasteSpecification.getWasteType();
		} else {
			line += "All waste types";
		}
		line += ";Others";
		if (subCategory != null) {
			line += subCategory;
		}
		line += ";";
		if (wasteSpecification.getComment() != null)
			line += comment(wasteSpecification.getComment());
		line += ";";
		return line;
	}

	/**
	 * Initializes the writer for the CSV file
	 * 
	 * @param fileName
	 *            The name of the file
	 * @param directory
	 *            The directory where the file should be written
	 * @throws IOException
	 */
	private void setUp(String fileName, File directory) throws IOException {
		File file = new File(directory.getAbsolutePath() + File.separator
				+ fileName + ".csv");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		writer = new BufferedWriter(new FileWriter(file));
	}

	/**
	 * Flushes and closes the writer
	 * 
	 * @throws IOException
	 */
	private void tearDown() throws IOException {
		writer.flush();
		writer.close();
	}

	/**
	 * Writes a calculated parameter into the CSV file
	 * 
	 * @param parameter
	 *            The calculated parameter to be written
	 * @throws IOException
	 */
	private void writeCalculatedParameter(SPCalculatedParameter parameter)
			throws IOException {
		String line = parameter.getName()
				+ ";"
				+ parameter.getExpression().replace(".",
						String.valueOf(separator)) + ";";
		if (parameter.getComment() != null)
			line += comment(parameter.getComment());
		writeln(line);
	}

	/**
	 * Writes a documentation of a data entry into the CSV file
	 * 
	 * @param documentation
	 *            The documentation to be written
	 * @param process
	 *            true if it is a process documentation, false if it is a waste
	 *            treatment documentation
	 */
	private void writeDocumentation(SPDocumentation documentation,
			boolean process) throws IOException {
		if (process) {
			writeEntry("Category type",
					documentation.getCategory() != null ? documentation
							.getCategory().getValue()
							: ProcessCategory.MATERIAL.getValue());
		} else {
			writeEntry("Category type",
					ProcessCategory.WASTE_TREATMENT.getValue());
		}
		writeEntry("Process identifier", null);
		writeEntry("Type", null);
		// TODO write not only UNIT_PROCESS
		// documentation.getProcessType() != null ? documentation
		// .getProcessType().getValue() : ProcessType.UNIT_PROCESS
		// .getValue());
		writeEntry("Process name", documentation.getName());
		writeEntry("Status", documentation.getStatus() != null ? documentation
				.getStatus().getValue() : Status.FINISHED.getValue());
		writeEntry(
				"Time period",
				documentation.getTimePeriod() != null ? documentation
						.getTimePeriod().getValue() : TimePeriod.UNSPECIFIED
						.getValue());
		writeEntry(
				"Geography",
				documentation.getGeography() != null ? documentation
						.getGeography().getValue() : Geography.UNSPECIFIED
						.getValue());
		writeEntry(
				"Technology",
				documentation.getTechnology() != null ? documentation
						.getTechnology().getValue() : Technology.UNSPECIFIED
						.getValue());
		writeEntry("Representativeness",
				documentation.getRepresentativeness() != null ? documentation
						.getRepresentativeness().getValue()
						: Representativeness.UNSPECIFIED.getValue());
		if (process) {
			writeEntry(
					"Multiple output allocation",
					documentation.getProcessAllocation() != null ? documentation
							.getProcessAllocation().getValue()
							: ProcessAllocation.UNSPECIFIED.getValue());
			writeEntry("Substitution allocation",
					documentation.getSubstitution() != null ? documentation
							.getSubstitution().getValue()
							: Substitution.UNSPECIFIED.getValue());
		} else {
			writeEntry(
					"Waste treatment allocation",
					documentation.getWasteTreatmentAllocation() != null ? documentation
							.getWasteTreatmentAllocation().getValue()
							: WasteTreatmentAllocation.UNSPECIFIED.getValue());
		}
		writeEntry(
				"Cut off rules",
				documentation.getCutOffRule() != null ? documentation
						.getCutOffRule().getValue() : CutOffRule.UNSPECIFIED
						.getValue());
		writeEntry("Capital goods",
				documentation.getSystemBoundary() != null ? documentation
						.getSystemBoundary().getValue()
						: SystemBoundary.UNSPECIFIED.getValue());
		writeEntry("Boundary with nature",
				documentation.getBoundaryWithNature() != null ? documentation
						.getBoundaryWithNature().getValue()
						: BoundaryWithNature.UNSPECIFIED.getValue());
		writeEntry("Infrastructure",
				documentation.isInfrastructureProcess() ? "Yes" : "No");
		writeEntry("Date", documentation.getCreationDate());
		writeEntry("Record", comment(documentation.getRecord()));
		writeEntry("Generator", comment(documentation.getGenerator()));
		writeln("Literature references");
		boolean atLeastOneReference = false;
		for (SPLiteratureReferenceEntry entry : documentation
				.getLiteratureReferencesEntries()) {
			String literatureReference = entry.getLiteratureReference() != null ? entry
					.getLiteratureReference().getName() : null;
			if (literatureReference != null && !literatureReference.equals("")
					&& entry.getComment() != null) {
				literatureReference += ";" + comment(entry.getComment());
			}
			if (literatureReference != null && !literatureReference.equals("")) {
				writeln(literatureReference);
				atLeastOneReference = true;
			}
		}
		if (!atLeastOneReference) {
			writer.newLine();
		}
		writer.newLine();
		writeEntry("Collection method",
				comment(documentation.getCollectionMethod()));
		writeEntry("Data treatment", comment(documentation.getDataTreatment()));
		writeEntry("Verification", comment(documentation.getVerification()));
		writeEntry("Comment", comment(documentation.getComment()));
		writeEntry("Allocation rules",
				comment(documentation.getAllocationRules()));
		String systemDescription = documentation.getSystemDescriptionEntry() != null ? documentation
				.getSystemDescriptionEntry().getSystemDescription().getName()
				: null;
		if (systemDescription != null
				&& !systemDescription.equals("")
				&& documentation.getSystemDescriptionEntry().getComment() != null) {
			systemDescription += ";"
					+ documentation.getSystemDescriptionEntry().getComment();
		}
		writeEntry("System description", comment(systemDescription));
	}

	/**
	 * Writes an entry into the CSV file
	 * 
	 * @param name
	 *            The name of the entry
	 * @param value
	 *            The value of the entry
	 */
	private void writeEntry(String name, String value) throws IOException {
		writeln(name);
		writeln(value != null ? value : "");
		writer.newLine();
	}

	/**
	 * Writes the header of the CSV file
	 * 
	 * @param project
	 *            The name of the project
	 * @throws IOException
	 */
	private void writeHeader(String project) throws IOException {
		writeln("{SimaPro 7.2}");
		writeln("{Processes}");
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
		writeln("{Date: " + dateFormat.format(new Date()) + "}");
		writeln("{Time: " + timeFormat.format(new Date()) + "}");
		writeln("{Project: " + project + "}");
		writeln("{CSV Format version: 7.0.0}");
		writeln("{CSV separator: Semicolon}");
		writeln("{Decimal separator: " + separator + "}");
		writeln("{Date separator: .}");
		writeln("{Short date format: dd.MM.yyyy}");
		writer.newLine();
	}

	/**
	 * Writes an input parameter into the CSV file
	 * 
	 * @param parameter
	 *            The input parameter to be written
	 * @throws IOException
	 */
	private void writeInputParameter(SPInputParameter parameter)
			throws IOException {
		String line = parameter.getName()
				+ ";"
				+ Double.toString(parameter.getValue()).replace(".",
						String.valueOf(separator)) + ";";
		line += getDistributionPart(parameter.getDistribution());
		line += (parameter.isHidden() ? "Yes" : "No") + ";";
		if (parameter.getComment() != null) {
			line += comment(parameter.getComment());
		}
		writeln(line);
	}

	/**
	 * Writes a line and jumps into the next line
	 * 
	 * @param line
	 *            The line to write
	 * @throws IOException
	 */
	private void writeln(String line) throws IOException {
		writer.write(line);
		writer.newLine();
	}

	/**
	 * Writes a process into the CSV file
	 * 
	 * @param process
	 *            The process to be written
	 */
	private void writeProcess(SPProcess process) throws IOException {
		writeln("Process");
		writer.newLine();
		if (process.getDocumentation() != null)
			writeDocumentation(process.getDocumentation(), true);

		writeln("Products");
		for (SPReferenceProduct product : process.getReferenceProducts()) {
			writeln(getProductLine(product, process.getSubCategory()));
		}
		writer.newLine();

		writeln("Avoided products");
		for (SPProductFlow product : process
				.getProductFlows(ProductFlowType.AVOIDED_PRODUCT)) {
			writeln(getProductLine(product));
		}
		writer.newLine();

		writeln("Resources");
		for (SPElementaryFlow flow : process
				.getElementaryFlows(ElementaryFlowType.RESOURCE)) {
			writeln(getElementaryFlowLine(flow));
		}
		writer.newLine();

		writeln("Materials/fuels");
		for (SPProductFlow product : process
				.getProductFlows(ProductFlowType.MATERIAL_INPUT)) {
			writeln(getProductLine(product));
		}
		writer.newLine();

		writeln("Electricity/heat");
		for (SPProductFlow product : process
				.getProductFlows(ProductFlowType.ELECTRICITY_INPUT)) {
			writeln(getProductLine(product));
		}
		writer.newLine();

		writeln("Emissions to air");
		for (SPElementaryFlow flow : process
				.getElementaryFlows(ElementaryFlowType.EMISSION_TO_AIR)) {
			writeln(getElementaryFlowLine(flow));
		}
		writer.newLine();

		writeln("Emissions to water");
		for (SPElementaryFlow flow : process
				.getElementaryFlows(ElementaryFlowType.EMISSION_TO_WATER)) {
			writeln(getElementaryFlowLine(flow));
		}
		writer.newLine();

		writeln("Emissions to soil");
		for (SPElementaryFlow flow : process
				.getElementaryFlows(ElementaryFlowType.EMISSION_TO_SOIL)) {
			writeln(getElementaryFlowLine(flow));
		}
		writer.newLine();

		writeln("Final waste flows");
		for (SPElementaryFlow flow : process
				.getElementaryFlows(ElementaryFlowType.FINAL_WASTE)) {
			writeln(getElementaryFlowLine(flow));
		}
		writer.newLine();

		writeln("Non material emissions");
		for (SPElementaryFlow flow : process
				.getElementaryFlows(ElementaryFlowType.NON_MATERIAL_EMISSIONS)) {
			writeln(getElementaryFlowLine(flow));
		}
		writer.newLine();

		writeln("Social issues");
		for (SPElementaryFlow flow : process
				.getElementaryFlows(ElementaryFlowType.SOCIAL_ISSUE)) {
			writeln(getElementaryFlowLine(flow));
		}
		writer.newLine();

		writeln("Economic issues");
		for (SPElementaryFlow flow : process
				.getElementaryFlows(ElementaryFlowType.ECONOMIC_ISSUE)) {
			writeln(getElementaryFlowLine(flow));
		}
		writer.newLine();

		writeln("Waste to treatment");
		for (SPWasteToTreatmentFlow flow : process.getWasteToTreatmentFlows()) {
			writeln(getWasteToTreatmentLine(flow));
		}
		writer.newLine();

		writeln("Input parameters");
		if (process.getInputParameters() != null
				&& process.getInputParameters().length > 0) {
			for (SPInputParameter parameter : process.getInputParameters()) {
				writeInputParameter(parameter);
			}
		} else {
			writer.newLine();
		}
		writer.newLine();

		writeln("Calculated parameters");
		if (process.getCalculatedParameters() != null
				&& process.getCalculatedParameters().length > 0) {
			for (SPCalculatedParameter parameter : process
					.getCalculatedParameters()) {
				writeCalculatedParameter(parameter);
			}
		} else {
			writer.newLine();
		}
		writer.newLine();

		writeln("End");
		writer.newLine();
	}

	/**
	 * Writes a quantity into the CSV file
	 * 
	 * @param quantity
	 *            The quantity to be written
	 * @throws IOException
	 */
	private void writeQuantity(SPQuantity quantity) throws IOException {
		writeln(quantity.getName() + ";"
				+ (quantity.isDimensional() ? "Yes" : "No"));
	}

	/**
	 * Writes a system description into the CSV file
	 * 
	 * @param systemDescription
	 *            The system description to be written
	 * @throws IOException
	 */
	private void writeSystemDescription(SPSystemDescription systemDescription)
			throws IOException {
		writeln("System description");
		writer.newLine();
		writeEntry("Name", systemDescription.getName());
		writeEntry(
				"Category",
				systemDescription.getCategory() != null ? systemDescription
						.getCategory() : "Others");
		writeEntry("Description", systemDescription.getDescription());
		writeEntry("Sub-systems", systemDescription.getSubSystems());
		writeEntry("Cut-off rules", systemDescription.getCutOffRules());
		writeEntry("Energy model", systemDescription.getEnergyModel());
		writeEntry("Transport model", systemDescription.getTransportModel());
		writeEntry("Waste model", systemDescription.getWasteModel());
		writeEntry("Other assumptions", systemDescription.getOtherAssumptions());
		writeEntry("Other information", systemDescription.getOtherInformation());
		writeEntry("Allocation rules", systemDescription.getAllocationRules());
		writeln("End");
		writer.newLine();
	}

	/**
	 * Writes a unit into the CSV file
	 * 
	 * @param unit
	 *            The unit to be written
	 * @throws IOException
	 */
	private void writeUnit(SPQuantity quantity, SPUnit unit) throws IOException {
		writeln(unit.getName() + ";" + quantity.getName() + ";"
				+ unit.getConversionFactor() + ";"
				+ quantity.getReferenceUnit().getName());
	}

	/**
	 * Writes a waste treatment into the CSV file
	 * 
	 * @param wasteTreatment
	 *            The waste treatment to be written
	 * @throws IOException
	 */
	private void writeWasteTreatment(SPWasteTreatment wasteTreatment)
			throws IOException {
		writeln("Process");
		writer.newLine();
		if (wasteTreatment.getDocumentation() != null)
			writeDocumentation(wasteTreatment.getDocumentation(), false);

		writeln("Waste treatment");
		writeln(getWasteSpecificationLine(
				wasteTreatment.getWasteSpecification(),
				wasteTreatment.getSubCategory()));
		writer.newLine();

		writeln("Avoided products");
		for (SPProductFlow product : wasteTreatment
				.getProductFlows(ProductFlowType.AVOIDED_PRODUCT)) {
			writeln(getProductLine(product));
		}
		writer.newLine();

		writeln("Resources");
		for (SPElementaryFlow flow : wasteTreatment
				.getElementaryFlows(ElementaryFlowType.RESOURCE)) {
			writeln(getElementaryFlowLine(flow));
		}
		writer.newLine();

		writeln("Materials/fuels");
		for (SPProductFlow product : wasteTreatment
				.getProductFlows(ProductFlowType.MATERIAL_INPUT)) {
			writeln(getProductLine(product));
		}
		writer.newLine();

		writeln("Electricity/heat");
		for (SPProductFlow product : wasteTreatment
				.getProductFlows(ProductFlowType.ELECTRICITY_INPUT)) {
			writeln(getProductLine(product));
		}
		writer.newLine();

		writeln("Emissions to air");
		for (SPElementaryFlow flow : wasteTreatment
				.getElementaryFlows(ElementaryFlowType.EMISSION_TO_AIR)) {
			writeln(getElementaryFlowLine(flow));
		}
		writer.newLine();

		writeln("Emissions to water");
		for (SPElementaryFlow flow : wasteTreatment
				.getElementaryFlows(ElementaryFlowType.EMISSION_TO_WATER)) {
			writeln(getElementaryFlowLine(flow));
		}
		writer.newLine();

		writeln("Emissions to soil");
		for (SPElementaryFlow flow : wasteTreatment
				.getElementaryFlows(ElementaryFlowType.EMISSION_TO_SOIL)) {
			writeln(getElementaryFlowLine(flow));
		}
		writer.newLine();

		writeln("Final waste flows");
		for (SPElementaryFlow flow : wasteTreatment
				.getElementaryFlows(ElementaryFlowType.FINAL_WASTE)) {
			writeln(getElementaryFlowLine(flow));
		}
		writer.newLine();

		writeln("Non material emissions");
		for (SPElementaryFlow flow : wasteTreatment
				.getElementaryFlows(ElementaryFlowType.NON_MATERIAL_EMISSIONS)) {
			writeln(getElementaryFlowLine(flow));
		}
		writer.newLine();

		writeln("Social issues");
		for (SPElementaryFlow flow : wasteTreatment
				.getElementaryFlows(ElementaryFlowType.SOCIAL_ISSUE)) {
			writeln(getElementaryFlowLine(flow));
		}
		writer.newLine();

		writeln("Economic issues");
		for (SPElementaryFlow flow : wasteTreatment
				.getElementaryFlows(ElementaryFlowType.ECONOMIC_ISSUE)) {
			writeln(getElementaryFlowLine(flow));
		}
		writer.newLine();

		writeln("Waste to treatment");
		for (SPWasteToTreatmentFlow flow : wasteTreatment
				.getWasteToTreatmentFlows()) {
			writeln(getWasteToTreatmentLine(flow));
		}
		writer.newLine();

		writeln("Input parameters");
		if (wasteTreatment.getInputParameters() != null
				&& wasteTreatment.getInputParameters().length > 0) {
			for (SPInputParameter parameter : wasteTreatment
					.getInputParameters()) {
				writeInputParameter(parameter);
			}
		} else {
			writer.newLine();
		}
		writer.newLine();
		writeln("End");
		writer.newLine();

		writeln("Calculated parameters");
		if (wasteTreatment.getCalculatedParameters() != null
				&& wasteTreatment.getCalculatedParameters().length > 0) {
			for (SPCalculatedParameter parameter : wasteTreatment
					.getCalculatedParameters()) {
				writeCalculatedParameter(parameter);
			}
		} else {
			writer.newLine();
		}
		writer.newLine();

		writeln("End");
		writer.newLine();
	}

	/**
	 * Set the decimal separator. Default: ','
	 * 
	 * @param separator
	 */
	public void setSeparator(char separator) {
		this.separator = separator;
	}

	/**
	 * Creates a new file with the given filename in the given directory and
	 * writes the given data set into it
	 * 
	 * @param directory
	 *            The directory where the file should be written
	 * @param dataSet
	 *            The data set to write
	 * @throws IOException
	 */
	public void write(File directory, SPDataSet dataSet) throws IOException {
		write(directory, dataSet, false, true);
	}

	/**
	 * Writes the given {@link SPDataSet}'s into one file which created from the
	 * first given {@link SPDataSet}. For the last given {@link SPDataSet} it is
	 * necessary that to set the boolean writeGeneralDate to true, because the
	 * writer will closed.
	 * 
	 * @param directory
	 *            The directory where the file should be written
	 * @param dataSet
	 *            The data set to write
	 * @throws IOException
	 */
	public void write(File directory, SPDataSet dataSet,
			boolean splitGivenDatasets, boolean writeGeneralData)
			throws IOException {
		boolean writeHeader = true;

		if (splitGivenDatasets) {
			if (writer == null) {
				setUp(dataSet.getProject(), directory);
			} else {
				writeHeader = false;
			}
		} else {
			setUp(dataSet.getProject(), directory);
		}

		if (writeHeader)
			writeHeader(dataSet.getProject());

		for (SPProcess process : dataSet.getProcesses()) {
			writeProcess(process);
		}

		for (SPWasteTreatment wasteTreatment : dataSet.getWasteTreatments()) {
			writeWasteTreatment(wasteTreatment);
		}

		if (writeGeneralData) {
			for (SPSystemDescription systemDescription : collectSystemDescriptions(dataSet)) {
				writeSystemDescription(systemDescription);
			}

			for (SPLiteratureReference literatureReference : collectLiteratureReferences(dataSet)) {
				writeLiteratureReference(literatureReference);
			}

			SPQuantity[] quantities = dataSet.getQuantities();
			if (quantities != null && quantities.length > 0) {
				writeln("Quantities");
				for (SPQuantity quantity : quantities) {
					writeQuantity(quantity);
				}
				writer.newLine();
				writeln("End");
				writer.newLine();

				writeln("Units");
				for (SPQuantity quantity : quantities) {
					for (SPUnit unit : quantity.getUnits()) {
						writeUnit(quantity, unit);
					}
				}
				writer.newLine();
				writeln("End");
				writer.newLine();
			}

			// TODO: eventually write substances

			writeEntry("Database Input parameters", null);
			writeln("End");
			writer.newLine();

			writeEntry("Database Calculated parameters", null);
			writeln("End");
			writer.newLine();

			writeln("Project Input parameters");
			if (dataSet.getInputParameters() != null
					&& dataSet.getInputParameters().length > 0) {
				for (SPInputParameter parameter : dataSet.getInputParameters()) {
					writeInputParameter(parameter);
				}
			} else {
				writer.newLine();
			}
			writeln("End");
			writer.newLine();

			writeln("Project Calculated parameters");
			if (dataSet.getCalculatedParameters() != null
					&& dataSet.getCalculatedParameters().length > 0) {
				for (SPCalculatedParameter parameter : dataSet
						.getCalculatedParameters()) {
					writeCalculatedParameter(parameter);
				}
			} else {
				writer.newLine();
			}
			writeln("End");
			writer.newLine();

			tearDown();
		}
	}
}
