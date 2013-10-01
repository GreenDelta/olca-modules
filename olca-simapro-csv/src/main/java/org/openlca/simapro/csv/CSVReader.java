package org.openlca.simapro.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.simapro.csv.model.IDistribution;
import org.openlca.simapro.csv.model.SPCalculatedParameter;
import org.openlca.simapro.csv.model.SPDataEntry;
import org.openlca.simapro.csv.model.SPDataSet;
import org.openlca.simapro.csv.model.SPDocumentation;
import org.openlca.simapro.csv.model.SPElementaryFlow;
import org.openlca.simapro.csv.model.SPInputParameter;
import org.openlca.simapro.csv.model.SPLiteratureReference;
import org.openlca.simapro.csv.model.SPLiteratureReferenceEntry;
import org.openlca.simapro.csv.model.SPLogNormalDistribution;
import org.openlca.simapro.csv.model.SPNormalDistribution;
import org.openlca.simapro.csv.model.SPPedigreeMatrix;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPProductFlow;
import org.openlca.simapro.csv.model.SPQuantity;
import org.openlca.simapro.csv.model.SPReferenceProduct;
import org.openlca.simapro.csv.model.SPSubstance;
import org.openlca.simapro.csv.model.SPSystemDescription;
import org.openlca.simapro.csv.model.SPSystemDescriptionEntry;
import org.openlca.simapro.csv.model.SPTriangleDistribution;
import org.openlca.simapro.csv.model.SPUniformDistribution;
import org.openlca.simapro.csv.model.SPUnit;
import org.openlca.simapro.csv.model.SPWasteSpecification;
import org.openlca.simapro.csv.model.SPWasteTreatment;
import org.openlca.simapro.csv.model.types.BoundaryWithNature;
import org.openlca.simapro.csv.model.types.CutOffRule;
import org.openlca.simapro.csv.model.types.ElementaryFlowType;
import org.openlca.simapro.csv.model.types.Geography;
import org.openlca.simapro.csv.model.types.ProcessAllocation;
import org.openlca.simapro.csv.model.types.ProcessCategory;
import org.openlca.simapro.csv.model.types.ProcessType;
import org.openlca.simapro.csv.model.types.ProductFlowType;
import org.openlca.simapro.csv.model.types.Representativeness;
import org.openlca.simapro.csv.model.types.Status;
import org.openlca.simapro.csv.model.types.SubCompartment;
import org.openlca.simapro.csv.model.types.Substitution;
import org.openlca.simapro.csv.model.types.SystemBoundary;
import org.openlca.simapro.csv.model.types.Technology;
import org.openlca.simapro.csv.model.types.TimePeriod;
import org.openlca.simapro.csv.model.types.WasteTreatmentAllocation;

/**
 * The CSV reader reads out a SimaPro 7.2 CSV file (file separator ;) and
 * creates a {@link SPDataSet} object containing the information of the file
 * 
 */
@Deprecated
public class CSVReader {

	/**
	 * The found literature references (key = name of the reference)
	 */
	private Map<String, SPLiteratureReference> literatureReferences = new HashMap<String, SPLiteratureReference>();

	/**
	 * The found quantites (key = name of the quantity)
	 */
	private Map<String, SPQuantity> quantities = new HashMap<String, SPQuantity>();

	/**
	 * The found substances (key = name of the substance)
	 */
	private Map<String, SPSubstance> substances = new HashMap<String, SPSubstance>();

	/**
	 * The found system description (key = name of the system)
	 */
	private Map<String, SPSystemDescription> systemDescriptions = new HashMap<String, SPSystemDescription>();

	/**
	 * The found units (key = name of the unit)
	 */
	private Map<String, SPUnit> units = new HashMap<String, SPUnit>();

	/**
	 * Creates an {@link IDistribution} object for the given values
	 * 
	 * @param type
	 *            The type of distribution
	 * @param value1
	 *            The first distribution value
	 * @param value2
	 *            The second distribution value
	 * @param value3
	 *            The third distribution value
	 * @return The right distribution object for the given values
	 */
	private IDistribution createDistibution(String type, String value1,
			String value2, String value3, String comment) {
		IDistribution distribution = null;
		if (type.toLowerCase().equals("lognormal")) {
			distribution = new SPLogNormalDistribution(
					Double.parseDouble(value1.replace(",", ".")),
					getPedigreeMatrix(comment));
		} else if (type.toLowerCase().equals("normal")) {
			distribution = new SPNormalDistribution(Double.parseDouble(value1
					.replace(",", ".")));
		} else if (type.toLowerCase().equals("triangle")) {
			distribution = new SPTriangleDistribution(Double.parseDouble(value2
					.replace(",", ".")), Double.parseDouble(value3.replace(",",
					".")));
		} else if (type.toLowerCase().equals("uniform")) {
			distribution = new SPUniformDistribution(Double.parseDouble(value2
					.replace(",", ".")), Double.parseDouble(value3.replace(",",
					".")));
		}
		return distribution;
	}

	private SPPedigreeMatrix getPedigreeMatrix(String comment) {
		SPPedigreeMatrix matrix = null;
		if (comment.startsWith("(") && comment.indexOf(")") == 12) {
			matrix = new SPPedigreeMatrix();
			String[] pedigree = comment.substring(1, comment.indexOf(")"))
					.split(",");
			matrix.setReliability(pedigree[0]);
			matrix.setCompleteness(pedigree[1]);
			matrix.setTemporalCorrelation(pedigree[2]);
			matrix.setGeographicalCorrelation(pedigree[3]);
			matrix.setFurtherTechnologicalCorrelation(pedigree[4]);
			matrix.setSampleSize(pedigree[5]);
		}
		return matrix;
	}

	/**
	 * Looks up the substance map for the given substance name, if no substance
	 * is found a new one will be created
	 * 
	 * @param name
	 *            The name of the substance
	 * @return The substance for the given name
	 */
	private SPSubstance getSubstance(String name) {
		SPSubstance substance = substances.get(name);
		if (substance == null) {
			substance = new SPSubstance(name, null, null);
			substances.put(name, substance);
		}
		return substance;
	}

	/**
	 * Looks up the unit map for the given unit name, if no substance is found a
	 * new one will be created
	 * 
	 * @param name
	 *            The name of the unit
	 * @return The unit for the given name
	 */
	private SPUnit getUnit(String name) {
		SPUnit unit = units.get(name);
		if (unit == null) {
			unit = new SPUnit(name, 1);
			units.put(name, unit);
		}
		return unit;
	}

	/**
	 * Reads the given line and creates a {@link SPCalculatedParameter} object
	 * 
	 * @param line
	 *            The line to read
	 * @return A calculated parameter with the information found in the given
	 *         line
	 */
	private SPCalculatedParameter readCalculatedParameter(String line) {
		line += "; ";
		String split[] = line.split(";");
		String name = split[0];
		String expression = split[1];
		String comment = split[2];

		for (int i = 3; i < (split.length - 1); i++) {
			comment += ";" + split[i];
		}

		return new SPCalculatedParameter(name, expression, comment);
	}

	/**
	 * Reads the information relevant for a {@link SPDataEntry}
	 * 
	 * @param reader
	 *            The reader of the file
	 * @return A new data entry containing the information found by the reader
	 * @throws IOException
	 */
	private SPDataEntry readDataEntry(BufferedReader reader) throws IOException {
		SPDataEntry entry = null;
		SPDocumentation documentation = readDocumentation(reader);
		// reader is at line after "Products" / "Waste treatment"
		if (documentation.getCategory() == ProcessCategory.WASTE_TREATMENT) {
			entry = readWasteTreatment(reader.readLine());
			reader.readLine(); // jump the empty row after the waste
			// specification entry to be at the same position
		} else {
			entry = readProcess(reader);
		}
		entry.setDocumentation(documentation);

		String line = null;
		while ((line = reader.readLine()) != null && !line.equals("End")) {
			if (line.equals("Avoided products")) {
				while ((line = reader.readLine()) != null && !line.equals("")) {
					entry.add(readProduct(line, ProductFlowType.AVOIDED_PRODUCT));
				}
			} else if (line.equals("Resources")) {
				while ((line = reader.readLine()) != null && !line.equals("")) {
					entry.add(readElementaryFlow(line,
							ElementaryFlowType.RESOURCE));
				}
			} else if (line.equals("Materials/fuels")) {
				while ((line = reader.readLine()) != null && !line.equals("")) {
					entry.add(readProduct(line, ProductFlowType.MATERIAL_INPUT));
				}
			} else if (line.equals("Electricity/heat")) {
				while ((line = reader.readLine()) != null && !line.equals("")) {
					entry.add(readProduct(line,
							ProductFlowType.ELECTRICITY_INPUT));
				}
			} else if (line.equals("Emissions to air")) {
				while ((line = reader.readLine()) != null && !line.equals("")) {
					entry.add(readElementaryFlow(line,
							ElementaryFlowType.EMISSION_TO_AIR));
				}
			} else if (line.equals("Emissions to water")) {
				while ((line = reader.readLine()) != null && !line.equals("")) {
					entry.add(readElementaryFlow(line,
							ElementaryFlowType.EMISSION_TO_WATER));
				}
			} else if (line.equals("Emissions to soil")) {
				while ((line = reader.readLine()) != null && !line.equals("")) {
					entry.add(readElementaryFlow(line,
							ElementaryFlowType.EMISSION_TO_SOIL));
				}
			} else if (line.equals("Final waste flows")) {
				while ((line = reader.readLine()) != null && !line.equals("")) {
					entry.add(readElementaryFlow(line,
							ElementaryFlowType.FINAL_WASTE));
				}
			} else if (line.equals("Non material emissions")) {
				while ((line = reader.readLine()) != null && !line.equals("")) {
					entry.add(readElementaryFlow(line,
							ElementaryFlowType.NON_MATERIAL_EMISSIONS));
				}
			} else if (line.equals("Social issues")) {
				while ((line = reader.readLine()) != null && !line.equals("")) {
					entry.add(readElementaryFlow(line,
							ElementaryFlowType.SOCIAL_ISSUE));
				}
			} else if (line.equals("Economic issues")) {
				while ((line = reader.readLine()) != null && !line.equals("")) {
					entry.add(readElementaryFlow(line,
							ElementaryFlowType.ECONOMIC_ISSUE));
				}
			} else if (line.equals("Waste to treatment")) {
				while ((line = reader.readLine()) != null && !line.equals("")) {
					entry.add(readProduct(line, ProductFlowType.WASTE_TREATMENT));
				}
			} else if (line.equals("Input parameters")) {
				while ((line = reader.readLine()) != null && !line.equals("")) {
					entry.add(readInputParameter(line));
				}
			} else if (line.equals("Calculated parameters")) {
				while ((line = reader.readLine()) != null && !line.equals("")) {
					entry.add(readCalculatedParameter(line));
				}
			}
		}
		return entry;
	}

	/**
	 * Reads the information relevant for a {@link SPDocumentation}
	 * 
	 * @param reader
	 *            The reader of the file
	 * @return A new documentation containing the information found by the
	 *         reader
	 * @throws IOException
	 */
	private SPDocumentation readDocumentation(BufferedReader reader)
			throws IOException {
		String line = null;
		ProcessCategory category = null;
		String identifier = null;
		ProcessType type = null;
		String name = null;
		Status status = null;
		TimePeriod period = null;
		Geography geography = null;
		Technology technology = null;
		Representativeness representativeness = null;
		ProcessAllocation processAllocation = null;
		Substitution substitution = null;
		CutOffRule rule = null;
		BoundaryWithNature boundaryWithNature = null;
		SystemBoundary systemBoundary = null;
		WasteTreatmentAllocation wasteAllocation = null;
		boolean infrastructure = false;
		String date = null;
		String record = null;
		String generator = null;
		String collectionMethod = null;
		String dataTreatment = null;
		String verification = null;
		String comment = null;
		String allocationRules = null;
		SPSystemDescriptionEntry systemDescriptionEntry = null;
		SPLiteratureReferenceEntry[] literatureReferenceEntries = null;
		while ((line = reader.readLine()) != null
				&& !(line.equals("Products") || line.equals("Waste treatment"))) {
			if (line.equals("Category type")) {
				category = ProcessCategory.forValue(reader.readLine().replace(
						";", ""));
			} else if (line.equals("Process identifier")) {
				identifier = reader.readLine();
			} else if (line.equals("Type")) {
				type = ProcessType.forValue(reader.readLine().replace(";", ""));
			} else if (line.equals("Process name")) {
				name = reader.readLine().replace(";", "");
			} else if (line.equals("Status")) {
				status = Status.forValue(reader.readLine().replace(";", ""));
			} else if (line.equals("Time period")) {
				period = TimePeriod
						.forValue(reader.readLine().replace(";", ""));
			} else if (line.equals("Geography")) {
				geography = Geography.forValue(reader.readLine().replace(";",
						""));
			} else if (line.equals("Technology")) {
				technology = Technology.forValue(reader.readLine().replace(";",
						""));
			} else if (line.equals("Representativeness")) {
				representativeness = Representativeness.forValue(reader
						.readLine().replace(";", ""));
			} else if (line.equals("Multiple output allocation")) {
				processAllocation = ProcessAllocation.forValue(reader
						.readLine().replace(";", ""));
			} else if (line.equals("Substitution allocation")) {
				substitution = Substitution.forValue(reader.readLine());
			} else if (line.equals("Cut off rules")) {
				rule = CutOffRule.forValue(reader.readLine().replace(";", ""));
			} else if (line.equals("Capital goods")) {
				systemBoundary = SystemBoundary.forValue(reader.readLine()
						.replace(";", ""));
			} else if (line.equals("Boundary with nature")) {
				boundaryWithNature = BoundaryWithNature.forValue(reader
						.readLine().replace(";", ""));
			} else if (line.equals("Waste treatment allocation")) {
				wasteAllocation = WasteTreatmentAllocation.forValue(reader
						.readLine().replace(";", ""));
			} else if (line.equals("Infrastructure")) {
				String iLine = reader.readLine().replace(";", "");
				infrastructure = iLine.equals("Yes") ? true : false;
			} else if (line.equals("Date")) {
				date = reader.readLine().replace(";", "");
			} else if (line.equals("Record")) {
				record = reader.readLine().replace(";", "");
			} else if (line.equals("Generator")) {
				generator = reader.readLine().replace(";", "");
			} else if (line.equals("Collection method")) {
				collectionMethod = reader.readLine().replace(";", "");
			} else if (line.equals("Data treatment")) {
				dataTreatment = reader.readLine().replace(";", "");
			} else if (line.equals("Verification")) {
				verification = reader.readLine().replace(";", "");
			} else if (line.equals("Comment")) {
				comment = reader.readLine().replace(";", "");
			} else if (line.equals("Allocation rules")) {
				allocationRules = reader.readLine().replace(";", "");
			} else if (line.equals("System description")) {
				systemDescriptionEntry = readSystemDescriptionEntry(reader
						.readLine());
			} else if (line.equals("Literature references")) {
				literatureReferenceEntries = readLiteratureReferenceEntries(reader);
			}
		}
		SPDocumentation documentation = new SPDocumentation(name, category,
				type);
		documentation.setIdentifier(identifier);
		documentation.setAllocationRules(allocationRules);
		documentation.setBoundaryWithNature(boundaryWithNature);
		documentation.setCollectionMethod(collectionMethod);
		documentation.setComment(comment);
		documentation.setCreationDate(date);
		documentation.setCutOffRule(rule);
		documentation.setDataTreatment(dataTreatment);
		documentation.setGenerator(generator);
		documentation.setGeography(geography);
		documentation.setInfrastructureProcess(infrastructure);
		documentation.setProcessAllocation(processAllocation);
		documentation.setRecord(record);
		documentation.setRepresentativeness(representativeness);
		documentation.setStatus(status);
		documentation.setSubstitution(substitution);
		documentation.setSystemBoundary(systemBoundary);
		documentation.setSystemDescriptionEntry(systemDescriptionEntry);
		documentation.setTechnology(technology);
		documentation.setTimePeriod(period);
		documentation.setVerification(verification);
		documentation.setWasteTreatmentAllocation(wasteAllocation);
		if (literatureReferenceEntries != null) {
			for (SPLiteratureReferenceEntry lrEntry : literatureReferenceEntries) {
				documentation.add(lrEntry);
			}
		}
		return documentation;
	}

	/**
	 * Reads the information relevant for a {@link SPElementaryFlow}
	 * 
	 * @param line
	 *            The line containing the flow information
	 * @param type
	 *            The type of elementary flow
	 * @return A new elementary flow containing the information found in the
	 *         line
	 * @throws IOException
	 */
	private SPElementaryFlow readElementaryFlow(String line,
			ElementaryFlowType type) {
		line += "; ";
		String split[] = line.split(";");
		String name = split[0];
		String subCompartment = split[1];
		String unit = split[2];
		String formula = split[3];
		String distribution = split[4];
		String dValue1 = split[5];
		String dValue2 = split[6];
		String dValue3 = split[7];
		String comment = split[8];

		for (int i = 9; i < (split.length - 1); i++) {
			comment += ";" + split[i];
		}

		return new SPElementaryFlow(type,
				SubCompartment.forValue(subCompartment), name, unit, formula,
				comment, createDistibution(distribution, dValue1, dValue2,
						dValue3, comment));
	}

	/**
	 * Reads the given line and creates a {@link SPInputParameter} object
	 * 
	 * @param line
	 *            The line to read
	 * @return An input parameter with the information found in the given line
	 */
	private SPInputParameter readInputParameter(String line) {
		line += "; ";
		String split[] = line.split(";");
		String name = split[0];
		String value = split[1];
		String distribution = split[2];
		String dValue1 = split[3];
		String dValue2 = split[4];
		String dValue3 = split[5];
		String hidden = split[6];
		String comment = split[7];

		for (int i = 8; i < (split.length - 1); i++) {
			comment += ";" + split[i];
		}

		return new SPInputParameter(name, Double.parseDouble(value.replace(',',
				'.')), createDistibution(distribution, dValue1, dValue2,
				dValue3, comment), comment, hidden.equals("Yes"));
	}

	/**
	 * Reads the information relevant for a {@link SPLiteratureReference} and
	 * stores it into the literature reference found in the map
	 * 
	 * @param reader
	 *            The reader of the file
	 * @throws IOException
	 */
	private void readLiteratureReference(BufferedReader reader)
			throws IOException {
		String line = null;
		String name = null;
		String category = null;
		String description = null;
		while ((line = reader.readLine()) != null && !line.equals("End")) {
			if (line.equals("Name")) {
				name = line.replace(";", "");
			} else if (line.equals("Category")) {
				category = line.replace(";", "");
			} else if (line.equals("Description")) {
				description = line.replace(";", "");
			}
		}
		SPLiteratureReference literatureReference = literatureReferences
				.get(name);
		if (literatureReference != null) {
			literatureReference.setCategory(category);
			literatureReference.setContent(description);
		}
	}

	/**
	 * Reads the literature references of a process
	 * 
	 * @param reader
	 *            The reader of the file
	 * @return The {@link SPLiteratureReferenceEntry}s for a process found by
	 *         the reader
	 * @throws IOException
	 */
	private SPLiteratureReferenceEntry[] readLiteratureReferenceEntries(
			BufferedReader reader) throws IOException {
		List<SPLiteratureReferenceEntry> entries = new ArrayList<SPLiteratureReferenceEntry>();
		String line = null;
		while ((line = reader.readLine()) != null && !line.equals("")) {
			String lrName = null;
			String lrComment = null;
			if (line.contains(";")) {
				lrName = line.substring(0, line.indexOf(';'));
				if (!line.endsWith(";")) {
					lrComment = line.substring(line.indexOf(';') + 1);
				}
			} else {
				lrName = line;
			}
			SPLiteratureReference literatureReference = literatureReferences
					.get(lrName);
			if (literatureReference == null) {
				literatureReference = new SPLiteratureReference(lrName, null,
						null);
				literatureReferences.put(lrName, literatureReference);
			}
			entries.add(new SPLiteratureReferenceEntry(literatureReference,
					lrComment));
		}
		return entries.toArray(new SPLiteratureReferenceEntry[entries.size()]);
	}

	/**
	 * Reads out a {@link SPProcess}
	 * 
	 * @param reader
	 *            The reader of the file
	 * @return The process found by the reader (only contains reference
	 *         products, no other information)
	 * @throws IOException
	 */
	private SPProcess readProcess(BufferedReader reader) throws IOException {
		List<SPReferenceProduct> referenceProducts = new ArrayList<SPReferenceProduct>();
		String subCategory = null;
		String line = null;

		while ((line = reader.readLine()) != null && !line.equals("")) {
			if (subCategory == null) {
				int semCounter = 0;
				subCategory = line;
				while (semCounter < 5 && subCategory.contains(";")) {
					subCategory = subCategory.substring(subCategory
							.indexOf(';') + 1);
					semCounter++;
				}
				if (subCategory.contains(";")) {
					subCategory = subCategory.substring(0,
							subCategory.indexOf(';'));
				}
			}
			referenceProducts.add(readReference(line));
		}

		SPProcess process = null;

		process = new SPProcess(referenceProducts.get(0), subCategory, null);
		for (int i = 1; i < referenceProducts.size(); i++) {
			process.add(referenceProducts.get(i));
		}

		return process;
	}

	/**
	 * Reads the information relevant for a {@link SPProductFlow}
	 * 
	 * @param line
	 *            The line containing the flow information
	 * @param type
	 *            The type of product
	 * @return A new product flow containing the information found in the line
	 * @throws IOException
	 */
	private SPProductFlow readProduct(String line, ProductFlowType type) {
		line += "; ";
		String split[] = line.split(";");
		String name = split[0];
		String unit = split[1];
		String formula = split[2];
		String distribution = split[3];
		String dValue1 = split[4];
		String dValue2 = split[5];
		String dValue3 = split[6];
		String comment = split[7];

		for (int i = 8; i < (split.length - 1); i++) {
			comment += ";" + split[i];
		}

		return new SPProductFlow(type, name, unit, formula, comment,
				createDistibution(distribution, dValue1, dValue2, dValue3,
						comment));
	}

	/**
	 * Reads out the project name
	 * 
	 * @param line
	 *            The line containing the project name
	 * @return The name of the project found in the line
	 */
	private String readProjectLine(String line) {
		String project = line.substring(line.indexOf(':') + 1);
		while (project.startsWith(" ")) {
			project = project.substring(1);
		}
		project = project.substring(0, project.indexOf('}'));
		return project;
	}

	/**
	 * Reads the quantity information and creates a new {@link SPQuantity} and
	 * stores it in the quantites map
	 * 
	 * @param line
	 *            The line containing the quantity information
	 * @return A new quantity with the given information
	 */
	private SPQuantity readQuantity(String line) {
		String name = line.substring(0, line.indexOf(';'));
		line = line.substring(0, line.indexOf(';'));
		String dimensional = line;
		if (dimensional.contains(";")) {
			dimensional = dimensional.substring(0, dimensional.indexOf(';'));
		}
		SPQuantity quantity = new SPQuantity(name, null);
		quantity.setDimensional(dimensional.equals("Yes"));
		quantities.put(name, quantity);
		return quantity;
	}

	/**
	 * Reads the information relevant for a {@link SPReferenceProduct}
	 * 
	 * @param line
	 *            The line containing the flow information
	 * @return A new reference product containing the information found in the
	 *         line
	 * @throws IOException
	 */
	private SPReferenceProduct readReference(String line) {
		line += "; ";
		String split[] = line.split(";");
		String name = split[0];
		String unit = split[1];
		String formula = split[2];
		String allocation = split[3];
		String wasteType = split[4];
		String category = split[5];
		String comment = split[6];

		for (int i = 7; i < (split.length - 1); i++) {
			comment += ";" + split[i];
		}

		return new SPReferenceProduct(name, unit, formula,
				Double.parseDouble(allocation.replace(',', '.')), wasteType,
				comment, category);
	}

	/**
	 * Reads the information relevant for a {@link SPSubstance} and stores it
	 * into the substance found in the map
	 * 
	 * @param line
	 *            The line containing the substance information
	 * @throws IOException
	 */
	private void readSubstance(String line) {
		line += "; ";
		String split[] = line.split(";");
		String name = split[0];
		String unit = split[1];
		String cas = split[2];
		String comment = split[3];

		for (int i = 4; i < (split.length - 1); i++) {
			comment += ";" + split[i];
		}

		SPSubstance substance = substances.get(name);
		if (substance != null) {
			substance.setCASNumber(cas);
			substance.setReferenceUnit(unit);
			substance.setComment(comment);
		}
	}

	private void readSystemDescription(BufferedReader reader)
			throws IOException {
		String line = null;
		String name = null;
		String category = null;
		String description = null;
		String subSystems = null;
		String cutOffRules = null;
		String energyModel = null;
		String transportModel = null;
		String wasteModel = null;
		String otherAssumptions = null;
		String otherInformation = null;
		String allocationRules = null;
		while ((line = reader.readLine()) != null && !line.equals("End")) {
			if (line.equals("Name")) {
				name = line.replace(";", "");
			} else if (line.equals("Category")) {
				category = line.replace(";", "");
			} else if (line.equals("Description")) {
				description = line.replace(";", "");
			} else if (line.equals("Sub-systems")) {
				description = line.replace(";", "");
			} else if (line.equals("Cut-off rules")) {
				description = line.replace(";", "");
			} else if (line.equals("Energy model")) {
				description = line.replace(";", "");
			} else if (line.equals("Transport model")) {
				description = line.replace(";", "");
			} else if (line.equals("Waste model")) {
				description = line.replace(";", "");
			} else if (line.equals("Other assumptions")) {
				description = line.replace(";", "");
			} else if (line.equals("Other information")) {
				description = line.replace(";", "");
			} else if (line.equals("Allocation rules")) {
				description = line.replace(";", "");
			}
		}
		SPSystemDescription systemDescription = systemDescriptions.get(name);
		if (systemDescription != null) {
			systemDescription.setAllocationRules(allocationRules);
			systemDescription.setCategory(category);
			systemDescription.setCutOffRules(cutOffRules);
			systemDescription.setDescription(description);
			systemDescription.setEnergyModel(energyModel);
			systemDescription.setOtherAssumptions(otherAssumptions);
			systemDescription.setOtherInformation(otherInformation);
			systemDescription.setSubSystems(subSystems);
			systemDescription.setTransportModel(transportModel);
			systemDescription.setWasteModel(wasteModel);
		}
	}

	/**
	 * Reads the system description of a process
	 * 
	 * @param line
	 *            The line containing the system description entry
	 * @return The {@link SPSystemDescriptionEntry}s for a process found in the
	 *         line
	 * @throws IOException
	 */
	private SPSystemDescriptionEntry readSystemDescriptionEntry(String line) {
		SPSystemDescriptionEntry entry = null;
		String sdName = null;
		String sdComment = null;
		if (line.contains(";")) {
			sdName = line.substring(0, line.indexOf(';'));
			if (!line.endsWith(";")) {
				sdComment = line.substring(line.indexOf(';') + 1);
			}
		} else {
			sdName = line;
		}
		if (!sdName.equals("")) {
			SPSystemDescription systemDescription = systemDescriptions
					.get(sdName);
			if (systemDescription == null) {
				systemDescription = new SPSystemDescription(sdName, null);
				systemDescriptions.put(sdName, systemDescription);
			}
			entry = new SPSystemDescriptionEntry(systemDescription, sdComment);
		}
		return entry;
	}

	/**
	 * Reads the information relevant for a {@link SPUnit} and stores it into
	 * the unit found in the map
	 * 
	 * @param line
	 *            The line containing the unit information
	 * @throws IOException
	 */
	private void readUnit(String line) {
		line += "; ";
		String split[] = line.split(";");
		String name = split[0];
		String quantityName = split[1];
		String conversionFactor = split[2];
		String referenceUnit = split[3];

		for (int i = 4; i < (split.length - 1); i++) {
			referenceUnit += ";" + split[i];
		}

		SPUnit unit = units.get(name);
		if (unit != null) {
			unit.setConversionFactor(Double.parseDouble(conversionFactor
					.replace(',', '.')));
			SPQuantity quantity = quantities.get(quantityName);
			if (quantity != null) {
				quantity.add(unit);
				if (referenceUnit.equals(name)) {
					quantity.setReferenceUnit(unit);
				}
			}
		}
	}

	/**
	 * Reads the information relevant for a {@link SPWasteSpecification}
	 * 
	 * @param line
	 *            The line containing the waste specification information
	 * @return A new waste specification containing the information found in the
	 *         line
	 * @throws IOException
	 */
	private SPWasteSpecification readWasteSpecification(String line) {
		line += "; ";
		String split[] = line.split(";");
		String name = split[0];
		String unit = split[1];
		String formula = split[2];
		String wasteType = split[3];
		String category = split[4];
		String comment = split[5];

		for (int i = 6; i < (split.length - 1); i++) {
			comment += ";" + split[i];
		}

		return new SPWasteSpecification(name, unit, formula, wasteType,
				comment, category);
	}

	/**
	 * Reads out a {@link SPWasteTreatment}
	 * 
	 * @param line
	 *            The line containing the waste specification information
	 * @return The waste treatment found in the line(only contains the waste
	 *         specification, no other information)
	 * @throws IOException
	 */
	private SPWasteTreatment readWasteTreatment(String line) {
		int semCounter = 0;
		String subCategory = line;
		while (semCounter < 4 && subCategory.contains(";")) {
			subCategory = subCategory.substring(subCategory.indexOf(';') + 1);
			semCounter++;
		}
		if (subCategory.contains(";")) {
			subCategory = subCategory.substring(0, subCategory.indexOf(';'));
		}
		return new SPWasteTreatment(readWasteSpecification(line), subCategory,
				null);
	}

	/**
	 * Reads the given CSV file and maps it into the SimaPro model
	 * 
	 * @param csvFile
	 *            The file to read
	 * @return A {@link SPDataSet} containing the information in the CSV file
	 * @throws IOException
	 */
	public SPDataSet read(File csvFile) throws IOException {
		return read(new FileReader(csvFile));
	}

	public SPDataSet read(Reader csvReader) throws IOException {
		BufferedReader reader = new BufferedReader(csvReader);
		String line = null;
		SPDataSet dataSet = new SPDataSet(null);
		while ((line = reader.readLine()) != null) {
			line = line.replace(((char) 127) + "", "/n");

			if (line.startsWith("{Project:")) {
				dataSet.setProject(readProjectLine(line));
			} else if (line.equals("Process")) {
				SPDataEntry entry = readDataEntry(reader);
				if (entry instanceof SPProcess) {
					dataSet.add((SPProcess) entry);
				} else if (entry instanceof SPWasteTreatment) {
					dataSet.add((SPWasteTreatment) entry);
				}
			} else if (line.equals("System description")) {
				readSystemDescription(reader);
			} else if (line.equals("Literature reference")) {
				readLiteratureReference(reader);
			} else if (line.equals("Database Input parameters")
					|| line.equals("Project Input parameters")) {
				while ((line = reader.readLine()) != null
						&& !(line.equals("") || line.equals("End"))) {
					dataSet.add(readInputParameter(line));
				}
			} else if (line.equals("Database Calculated parameters")
					|| line.equals("Project Calculated parameters")) {
				while ((line = reader.readLine()) != null
						&& !(line.equals("") || line.equals("End"))) {
					dataSet.add(readCalculatedParameter(line));
				}
			} else if (line.equals("Quantities")) {
				while ((line = reader.readLine()) != null
						&& !(line.equals("") || line.equals("End"))) {
					dataSet.add(readQuantity(line));
				}
			} else if (line.equals("Units")) {
				while ((line = reader.readLine()) != null
						&& !(line.equals("") || line.equals("End"))) {
					readUnit(line);
				}
			} else if (line.equals("Raw materials")
					|| line.equals("Airborne emissions")
					|| line.equals("Waterborne emissions")
					|| line.equals("Final waste flows")
					|| line.equals("Emissions to soil")
					|| line.equals("Non material emissions")) {
				while ((line = reader.readLine()) != null
						&& !(line.equals("") || line.equals("End"))) {
					readSubstance(line);
				}
			}
		}
		return dataSet;
	}
}
