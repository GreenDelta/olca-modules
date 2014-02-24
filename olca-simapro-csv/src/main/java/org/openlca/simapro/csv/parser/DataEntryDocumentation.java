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
		while (!lines.isEmpty()
				&& !(lines.peek().equals("Products") || lines.peek().equals(
						"Waste treatment"))) {
			String switchLine = lines.poll();
			String valueLine = Utils.replaceCSVSeperator(lines.peek(),
					csvSeperator);
			switch (switchLine) {
			case "Category type":
				documentation.setCategory(ProcessCategory.forValue(valueLine));
				lines.remove();
				break;
			case "Process identifier":
				documentation.setIdentifier(valueLine);
				lines.remove();
				break;
			case "Type":
				documentation.setProcessType(ProcessType.forValue(valueLine));
				lines.remove();
				break;
			case "Process name":
				documentation.setName(lines.poll());
				break;
			case "Status":
				documentation.setStatus(Status.forValue(valueLine));
				lines.remove();
				break;
			case "Time period":
				documentation.setTimePeriod(TimePeriod.forValue(valueLine));
				lines.remove();
				break;
			case "Geography":
				documentation.setGeography(Geography.forValue(valueLine));
				lines.remove();
				break;
			case "Technology":
				documentation.setTechnology(Technology.forValue(valueLine));
				lines.remove();
				break;
			case "Representativeness":
				documentation.setRepresentativeness(Representativeness
						.forValue(valueLine));
				lines.remove();
				break;
			case "Multiple output allocation":
				documentation.setProcessAllocation(ProcessAllocation
						.forValue(valueLine));
				lines.remove();
				break;
			case "Substitution allocation":
				documentation.setSubstitution(Substitution.forValue(valueLine));
				lines.remove();
				break;
			case "Cut off rules":
				documentation.setCutOffRule(CutOffRule.forValue(valueLine));
				lines.remove();
				break;
			case "Capital goods":
				documentation.setSystemBoundary(SystemBoundary
						.forValue(valueLine));
				lines.remove();
				break;
			case "Boundary with nature":
				documentation.setBoundaryWithNature(BoundaryWithNature
						.forValue(valueLine));
				lines.remove();
				break;
			case "Waste treatment allocation":
				documentation
						.setWasteTreatmentAllocation(WasteTreatmentAllocation
								.forValue(valueLine));
				lines.remove();
				break;
			case "Infrastructure":
				documentation
						.setInfrastructureProcess(valueLine.equals("Yes") ? true
								: false);
				lines.remove();
				break;
			case "Date":
				documentation.setCreationDate(valueLine);
				lines.remove();
				break;
			case "Record":
				documentation.setRecord(valueLine);
				lines.remove();
				break;
			case "Generator":
				documentation.setGenerator(valueLine);
				lines.remove();
				break;
			case "Collection method":
				documentation.setCollectionMethod(valueLine);
				lines.remove();
				break;
			case "Data treatment":
				documentation.setDataTreatment(valueLine);
				lines.remove();
				break;
			case "Verification":
				documentation.setVerification(valueLine);
				lines.remove();
				break;
			case "Comment":
				documentation.setComment(lines.poll());
				lines.remove();
				break;
			case "Allocation rules":
				documentation.setAllocationRules(lines.poll());
				lines.remove();
				break;
			case "System description":
				systemDescriptionEntry(lines.poll(), documentation);
				break;
			case "Literature references":
				literatureReferenceEntries(lines, documentation);
				break;
			}
		}
		return documentation;
	}
}
