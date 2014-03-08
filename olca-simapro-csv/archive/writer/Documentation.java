package org.openlca.simapro.csv.writer;

import static org.openlca.simapro.csv.writer.WriterUtils.comment;

import java.io.IOException;

import org.openlca.simapro.csv.model.SPProcessDocumentation;
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

class Documentation {

	void write(SPProcessDocumentation documentation, boolean process,
			CSVWriter writer) throws IOException {
		if (process) {
			writer.writeEntry("Category type",
					documentation.getCategory() != null ? documentation
							.getCategory().getValue()
							: ProcessCategory.MATERIAL.getValue());
		} else {
			writer.writeEntry("Category type",
					ProcessCategory.WASTE_TREATMENT.getValue());
		}
		writer.writeEntry(
				"Type",
				documentation.getProcessType() != null ? documentation
						.getProcessType().getValue() : ProcessType.UNIT_PROCESS
						.getValue());
		writer.writeEntry("Process name", documentation.getName());
		writer.writeEntry("Status",
				documentation.getStatus() != null ? documentation.getStatus()
						.getValue() : Status.FINISHED.getValue());
		writer.writeEntry(
				"Time period",
				documentation.getTimePeriod() != null ? documentation
						.getTimePeriod().getValue() : TimePeriod.UNSPECIFIED
						.getValue());
		writer.writeEntry(
				"Geography",
				documentation.getGeography() != null ? documentation
						.getGeography().getValue() : Geography.UNSPECIFIED
						.getValue());
		writer.writeEntry(
				"Technology",
				documentation.getTechnology() != null ? documentation
						.getTechnology().getValue() : Technology.UNSPECIFIED
						.getValue());
		writer.writeEntry("Representativeness", documentation
				.getRepresentativeness() != null ? documentation
				.getRepresentativeness().getValue()
				: Representativeness.UNSPECIFIED.getValue());
		if (process) {
			writer.writeEntry("Multiple output allocation", documentation
					.getProcessAllocation() != null ? documentation
					.getProcessAllocation().getValue()
					: ProcessAllocation.UNSPECIFIED.getValue());
			writer.writeEntry("Substitution allocation",
					documentation.getSubstitution() != null ? documentation
							.getSubstitution().getValue()
							: Substitution.UNSPECIFIED.getValue());
		} else {
			writer.writeEntry("Waste treatment allocation", documentation
					.getWasteTreatmentAllocation() != null ? documentation
					.getWasteTreatmentAllocation().getValue()
					: WasteTreatmentAllocation.UNSPECIFIED.getValue());
		}
		writer.writeEntry(
				"Cut off rules",
				documentation.getCutOffRule() != null ? documentation
						.getCutOffRule().getValue() : CutOffRule.UNSPECIFIED
						.getValue());
		writer.writeEntry("Capital goods",
				documentation.getSystemBoundary() != null ? documentation
						.getSystemBoundary().getValue()
						: SystemBoundary.UNSPECIFIED.getValue());
		writer.writeEntry("Boundary with nature", documentation
				.getBoundaryWithNature() != null ? documentation
				.getBoundaryWithNature().getValue()
				: BoundaryWithNature.UNSPECIFIED.getValue());
		writer.writeEntry("Infrastructure",
				documentation.isInfrastructureProcess() ? "Yes" : "No");
		writer.writeEntry("Date", documentation.getCreationDate());
		writer.writeEntry("Record", comment(documentation.getRecord()));
		writer.writeEntry("Generator", comment(documentation.getGenerator()));
		writer.writeln("Literature references");
		boolean atLeastOneReference = false;
		for (LiteratureReferenceRow entry : documentation
				.getLiteratureReferenceEntries()) {
			String literatureReference = entry.getLiteratureReference() != null ? entry
					.getLiteratureReference().getName() : null;
			if (literatureReference != null && !literatureReference.equals("")
					&& entry.getComment() != null) {
				literatureReference += writer.getSeparator()
						+ comment(entry.getComment());
			}
			if (literatureReference != null && !literatureReference.equals("")) {
				writer.writeln(literatureReference);
				atLeastOneReference = true;
			}
		}
		if (!atLeastOneReference) {
			writer.newLine();
		}
		writer.newLine();
		writer.writeEntry("Collection method",
				comment(documentation.getCollectionMethod()));
		writer.writeEntry("Data treatment",
				comment(documentation.getDataTreatment()));
		writer.writeEntry("Verification",
				comment(documentation.getVerification()));
		writer.writeEntry("Comment", comment(documentation.getComment()));
		writer.writeEntry("Allocation rules",
				comment(documentation.getAllocationRules()));
		String systemDescription = documentation.getSystemDescriptionEntry() != null ? documentation
				.getSystemDescriptionEntry().getSystemDescription().getName()
				: null;
		if (systemDescription != null
				&& !systemDescription.equals("")
				&& documentation.getSystemDescriptionEntry().getComment() != null) {
			systemDescription += writer.getSeparator()
					+ documentation.getSystemDescriptionEntry().getComment();
		}
		if (systemDescription != null && !systemDescription.equals(""))
			writer.writeEntry("System description", comment(systemDescription));
	}

}
