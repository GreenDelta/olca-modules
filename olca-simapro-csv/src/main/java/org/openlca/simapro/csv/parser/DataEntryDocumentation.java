package org.openlca.simapro.csv.parser;

import java.util.Queue;

import org.openlca.simapro.csv.model.SPLiteratureReference;
import org.openlca.simapro.csv.model.SPLiteratureReferenceEntry;
import org.openlca.simapro.csv.model.SPProcessDocumentation;
import org.openlca.simapro.csv.model.SPReferenceData;
import org.openlca.simapro.csv.model.SPSystemDescription;
import org.openlca.simapro.csv.model.SPSystemDescriptionEntry;
import org.openlca.simapro.csv.model.enums.BoundaryWithNature;
import org.openlca.simapro.csv.model.enums.CutOffRule;
import org.openlca.simapro.csv.model.enums.Geography;
import org.openlca.simapro.csv.model.enums.ProcessAllocation;
import org.openlca.simapro.csv.model.enums.ProcessCategory;
import org.openlca.simapro.csv.model.enums.ProcessType;
import org.openlca.simapro.csv.model.enums.Representativeness;
import org.openlca.simapro.csv.model.enums.Status;
import org.openlca.simapro.csv.model.enums.Substitution;
import org.openlca.simapro.csv.model.enums.SystemBoundary;
import org.openlca.simapro.csv.model.enums.Technology;
import org.openlca.simapro.csv.model.enums.TimePeriod;
import org.openlca.simapro.csv.model.enums.WasteTreatmentAllocation;

public class DataEntryDocumentation {

	private String csvSeperator;
	private SPReferenceData referenceData;

	DataEntryDocumentation(String csvSeperator, SPReferenceData referenceData) {
		this.csvSeperator = csvSeperator;
		this.referenceData = referenceData;
	}

	private void systemDescriptionEntry(String line,
			SPProcessDocumentation documentation) {
		SPSystemDescriptionEntry entry = null;
		String sdName = null;
		String sdComment = null;
		if (line.contains(csvSeperator)) {
			sdName = line.substring(0, line.indexOf(csvSeperator));
			if (!line.endsWith(csvSeperator))
				sdComment = line.substring(line.indexOf(csvSeperator) + 1);
		} else {
			sdName = line;
		}
		SPSystemDescription systemDescription = null;
		if (!sdName.equals("")) {
			systemDescription = new SPSystemDescription(sdName, null);
			entry = new SPSystemDescriptionEntry(systemDescription, sdComment);
			documentation.setSystemDescriptionEntry(entry);
		}
	}

	private void literatureReferenceEntries(Queue<String> lines,
			SPProcessDocumentation documentation) {
		while (!lines.isEmpty() && !lines.peek().equals("")) {
			String line = lines.poll();
			String name = null;
			String comment = null;
			if (line.contains(csvSeperator)) {
				name = line.substring(0, line.indexOf(csvSeperator));
				if (!line.endsWith(csvSeperator)) {
					comment = line.substring(line.indexOf(csvSeperator) + 1);
				}
			} else {
				name = line;
			}
			SPLiteratureReference literatureReference = referenceData
					.getLiteratureReferences().get(name);
			if (literatureReference == null)
				literatureReference = new SPLiteratureReference(name, null,
						null);
			documentation.getLiteratureReferenceEntries()
					.add(new SPLiteratureReferenceEntry(literatureReference,
							comment));
		}
	}

	SPProcessDocumentation parse(Queue<String> lines) {
		SPProcessDocumentation documentation = new SPProcessDocumentation(null,
				null, null);
		while (!lines.isEmpty()) {
			if (lines.peek().equals("Products"))
				break;
			if (lines.peek().equals("Waste treatment"))
				break;

			String headerLine = lines.poll();
			if (lines.isEmpty())
				break;
			String valueLine = lines.poll();
			if (headerLine == null || valueLine == null)
				break;

			valueLine = valueLine.replaceAll(csvSeperator, ""); // TODO: why?

			switch (headerLine) {
			case "Category type":
				documentation.setCategory(ProcessCategory.forValue(valueLine));
				break;
			case "Process identifier":
				documentation.setIdentifier(valueLine);
				break;
			case "Type":
				documentation.setProcessType(ProcessType.forValue(valueLine));
				break;
			case "Process name":
				documentation.setName(valueLine);
				break;
			case "Status":
				documentation.setStatus(Status.forValue(valueLine));
				break;
			case "Time period":
				documentation.setTimePeriod(TimePeriod.forValue(valueLine));
				break;
			case "Geography":
				documentation.setGeography(Geography.forValue(valueLine));
				break;
			case "Technology":
				documentation.setTechnology(Technology.forValue(valueLine));
				break;
			case "Representativeness":
				documentation.setRepresentativeness(Representativeness
						.forValue(valueLine));
				break;
			case "Multiple output allocation":
				documentation.setProcessAllocation(ProcessAllocation
						.forValue(valueLine));
				break;
			case "Substitution allocation":
				documentation.setSubstitution(Substitution.forValue(valueLine));
				break;
			case "Cut off rules":
				documentation.setCutOffRule(CutOffRule.forValue(valueLine));
				break;
			case "Capital goods":
				documentation.setSystemBoundary(SystemBoundary
						.forValue(valueLine));
				break;
			case "Boundary with nature":
				documentation.setBoundaryWithNature(BoundaryWithNature
						.forValue(valueLine));
				break;
			case "Waste treatment allocation":
				documentation
						.setWasteTreatmentAllocation(WasteTreatmentAllocation
								.forValue(valueLine));
				break;
			case "Infrastructure":
				documentation
						.setInfrastructureProcess(valueLine.equals("Yes") ? true
								: false);
				break;
			case "Date":
				documentation.setCreationDate(valueLine);
				break;
			case "Record":
				documentation.setRecord(valueLine);
				break;
			case "Generator":
				documentation.setGenerator(valueLine);
				break;
			case "Collection method":
				documentation.setCollectionMethod(valueLine);
				break;
			case "Data treatment":
				documentation.setDataTreatment(valueLine);
				break;
			case "Verification":
				documentation.setVerification(valueLine);
				break;
			case "Comment":
				documentation.setComment(valueLine);
				break;
			case "Allocation rules":
				documentation.setAllocationRules(valueLine);
				break;
			case "System description":
				systemDescriptionEntry(valueLine, documentation);
				break;
			case "Literature references":
				literatureReferenceEntries(lines, documentation);
				break;
			}
		}
		return documentation;
	}
}
