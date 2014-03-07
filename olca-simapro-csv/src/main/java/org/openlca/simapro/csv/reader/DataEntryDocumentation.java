package org.openlca.simapro.csv.reader;

import java.util.Queue;

import org.openlca.simapro.csv.model.SPProcessDocumentation;
import org.openlca.simapro.csv.model.SPReferenceData;
import org.openlca.simapro.csv.model.SPSystemDescription;
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
import org.openlca.simapro.csv.model.process.LiteratureReferenceRow;
import org.openlca.simapro.csv.model.process.SystemDescriptionRow;
import org.openlca.simapro.csv.model.refdata.LiteratureReferenceBlock;

public class DataEntryDocumentation {

	private String csvSeperator;
	private SPReferenceData refData;

	DataEntryDocumentation(String csvSeperator, SPReferenceData referenceData) {
		this.csvSeperator = csvSeperator;
		this.refData = referenceData;
	}

	private void systemDescriptionEntry(String line,
			SPProcessDocumentation documentation) {
		SystemDescriptionRow entry = null;
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
			entry = new SystemDescriptionRow(systemDescription, sdComment);
			documentation.setSystemDescriptionEntry(entry);
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

			switch (headerLine) {
			case "Category type":
				documentation
						.setCategory(ProcessCategory.forValue(lines.poll()));
				break;
			case "Process identifier":
				documentation.setIdentifier(lines.poll());
				break;
			case "Type":
				documentation
						.setProcessType(ProcessType.forValue(lines.poll()));
				break;
			case "Process name":
				documentation.setName(lines.poll());
				break;
			case "Status":
				documentation.setStatus(Status.forValue(lines.poll()));
				break;
			case "Time period":
				documentation.setTimePeriod(TimePeriod.forValue(lines.poll()));
				break;
			case "Geography":
				documentation.setGeography(Geography.forValue(lines.poll()));
				break;
			case "Technology":
				documentation.setTechnology(Technology.forValue(lines.poll()));
				break;
			case "Representativeness":
				documentation.setRepresentativeness(Representativeness
						.forValue(lines.poll()));
				break;
			case "Multiple output allocation":
				documentation.setProcessAllocation(ProcessAllocation
						.forValue(lines.poll()));
				break;
			case "Substitution allocation":
				documentation.setSubstitution(Substitution.forValue(lines
						.poll()));
				break;
			case "Cut off rules":
				documentation.setCutOffRule(CutOffRule.forValue(lines.poll()));
				break;
			case "Capital goods":
				documentation.setSystemBoundary(SystemBoundary.forValue(lines
						.poll()));
				break;
			case "Boundary with nature":
				documentation.setBoundaryWithNature(BoundaryWithNature
						.forValue(lines.poll()));
				break;
			case "Waste treatment allocation":
				documentation
						.setWasteTreatmentAllocation(WasteTreatmentAllocation
								.forValue(lines.poll()));
				break;
			case "Infrastructure":
				documentation.setInfrastructureProcess(lines.poll().equals(
						"Yes") ? true : false);
				break;
			case "Date":
				documentation.setCreationDate(lines.poll());
				break;
			case "Record":
				documentation.setRecord(lines.poll());
				break;
			case "Generator":
				documentation.setGenerator(lines.poll());
				break;
			case "Collection method":
				documentation.setCollectionMethod(lines.poll());
				break;
			case "Data treatment":
				documentation.setDataTreatment(lines.poll());
				break;
			case "Verification":
				documentation.setVerification(lines.poll());
				break;
			case "Comment":
				documentation.setComment(lines.poll());
				break;
			case "Allocation rules":
				documentation.setAllocationRules(lines.poll());
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
			LiteratureReferenceBlock reference = refData.getLiteratureReferences()
					.get(name);
			if (reference == null)
				reference = new LiteratureReferenceBlock(name, null, null);
			documentation.getLiteratureReferenceEntries().add(
					new LiteratureReferenceRow(reference, comment));
		}
	}
}
