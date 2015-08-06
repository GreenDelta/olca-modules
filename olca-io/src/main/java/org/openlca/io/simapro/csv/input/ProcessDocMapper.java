package org.openlca.io.simapro.csv.input;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.openlca.simapro.csv.model.enums.ValueEnum;
import org.openlca.simapro.csv.model.process.LiteratureReferenceRow;
import org.openlca.simapro.csv.model.process.ProcessBlock;
import org.openlca.simapro.csv.model.process.SystemDescriptionRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessDocMapper {

	private RefData refData;

	private ProcessBlock block;
	private Process process;

	public ProcessDocMapper(IDatabase database, RefData refData) {
		this.refData = refData;
	}

	public void map(ProcessBlock block, Process process) {
		this.block = block;
		this.process = process;
		if (process.getDocumentation() == null)
			process.setDocumentation(new ProcessDocumentation());
		mapSources();
		mapDocFields();
		mapDescription();
		if (block.getInfrastructure() != null)
			process.setInfrastructureProcess(block.getInfrastructure());
	}

	private void mapSources() {
		ProcessDocumentation doc = process.getDocumentation();
		for (LiteratureReferenceRow row : block.getLiteratureReferences()) {
			Source source = refData.getSource(row.getName());
			if (source == null)
				continue;
			doc.getSources().add(source);
		}
	}

	private void mapDocFields() {
		ProcessDocumentation doc = process.getDocumentation();
		mapTime(doc);
		if (block.getGeography() != null)
			doc.setGeography(block.getGeography().getValue());
		if (block.getTechnology() != null)
			doc.setTechnology(block.getTechnology().getValue());
		if (block.getRepresentativeness() != null)
			doc.setDataSelection(block.getRepresentativeness().getValue());
		doc.setDataTreatment(block.getDataTreatment());
		doc.setSampling(block.getCollectionMethod());
		doc.setReviewDetails(block.getVerification());
		mapInventoryMethod(doc);
		mapCompleteness(doc);
		mapProject(doc);
		doc.setCreationDate(block.getDate());
	}

	private void mapInventoryMethod(ProcessDocumentation doc) {
		String t = null;
		t = a("Allocation rules", block.getAllocationRules(), t);
		t = a("Multiple output allocation", block.getAllocation(), t);
		t = a("Substitution allocation", block.getSubstitution(), t);
		doc.setInventoryMethod(t);
	}

	private void mapCompleteness(ProcessDocumentation doc) {
		String t = null;
		t = a("Cut off rules", block.getCutoff(), t);
		t = a("Capital goods", block.getCapitalGoods(), t);
		doc.setCompleteness(t);
	}

	private void mapProject(ProcessDocumentation doc) {
		SystemDescriptionRow r = block.getSystemDescription();
		if (r == null || r.getName() == null)
			return;
		String t = r.getName();
		if (r.getComment() != null)
			t += " (" + r.getComment() + ")";
		doc.setProject(t);
	}

	private void mapTime(ProcessDocumentation doc) {
		if (block.getTime() == null)
			return;
		String text = block.getTime().getValue();
		Pattern pattern = Pattern.compile("(\\d{4})-(\\d{4})");
		Matcher m = pattern.matcher(text);
		if (!m.matches()) {
			doc.setTime(text);
			return;
		}
		try {
			int startYear = Integer.parseInt(m.group(1));
			Calendar c = Calendar.getInstance();
			c.set(startYear, 0, 1, 0, 0);
			doc.setValidFrom(c.getTime());
			int endYear = Integer.parseInt(m.group(2));
			c.set(endYear, 11, 31, 0, 0);
			doc.setValidUntil(c.getTime());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to convert time", e);
		}
	}

	private void mapDescription() {
		StringBuilder builder = new StringBuilder();
		if (block.getComment() != null)
			builder.append(block.getComment());
		a("Status", block.getStatus(), builder);
		a("Boundary with nature", block.getBoundaryWithNature(), builder);
		a("Record", block.getRecord(), builder);
		a("Generator", block.getGenerator(), builder);
		process.setDescription(builder.toString());
	}

	private void a(String label, String value, StringBuilder builder) {
		if (value == null)
			return;
		builder.append(label).append(": ").append(value).append("\n");
	}

	private String a(String label, String value, String field) {
		if (value == null)
			return field;
		if (field == null)
			return label + ": " + value + "\n";
		else
			return field + label + ": " + value + "\n";
	}

	private String a(String label, ValueEnum venum, String field) {
		if (venum == null)
			return field;
		if (field == null)
			return label + ": " + venum.getValue() + "\n";
		else
			return field + label + ": " + venum.getValue() + "\n";
	}

	private void a(String label, ValueEnum venum, StringBuilder builder) {
		if (venum == null)
			return;
		builder.append(label).append(": ").append(venum.getValue())
				.append("\n");
	}

}
