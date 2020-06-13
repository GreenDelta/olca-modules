package org.openlca.io.simapro.csv.input;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private final RefData refData;

	private ProcessBlock block;
	private Process process;

	public ProcessDocMapper(RefData refData) {
		this.refData = refData;
	}

	public void map(ProcessBlock block, Process process) {
		this.block = block;
		this.process = process;
		if (process.documentation == null)
			process.documentation = new ProcessDocumentation();
		mapSources();
		mapDocFields();
		mapDescription();
		if (block.infrastructure != null)
			process.infrastructureProcess = block.infrastructure;
	}

	private void mapSources() {
		ProcessDocumentation doc = process.documentation;
		for (LiteratureReferenceRow row : block.literatureReferences) {
			Source source = refData.getSource(row.name);
			if (source == null)
				continue;
			doc.sources.add(source);
		}
	}

	private void mapDocFields() {
		ProcessDocumentation doc = process.documentation;
		mapTime(doc);
		if (block.geography != null)
			doc.geography = block.geography.getValue();
		if (block.technology != null)
			doc.technology = block.technology.getValue();
		if (block.representativeness != null)
			doc.dataSelection = block.representativeness.getValue();
		doc.dataTreatment = block.dataTreatment;
		doc.sampling = block.collectionMethod;
		doc.reviewDetails = block.verification;
		mapInventoryMethod(doc);
		mapCompleteness(doc);
		mapProject(doc);
		doc.creationDate = block.date;
	}

	private void mapInventoryMethod(ProcessDocumentation doc) {
		var t = a("Allocation rules", block.allocationRules, (String) null);
		t = a("Multiple output allocation", block.allocation, t);
		t = a("Substitution allocation", block.substitution, t);
		doc.inventoryMethod = t;
	}

	private void mapCompleteness(ProcessDocumentation doc) {
		String t = a("Cut off rules", block.cutoff, (String) null);
		t = a("Capital goods", block.capitalGoods, t);
		doc.completeness = t;
	}

	private void mapProject(ProcessDocumentation doc) {
		SystemDescriptionRow r = block.systemDescription;
		if (r == null || r.getName() == null)
			return;
		String t = r.getName();
		if (r.getComment() != null)
			t += " (" + r.getComment() + ")";
		doc.project = t;
	}

	private void mapTime(ProcessDocumentation doc) {
		if (block.time == null)
			return;
		String text = block.time.getValue();
		Pattern pattern = Pattern.compile("(\\d{4})-(\\d{4})");
		Matcher m = pattern.matcher(text);
		if (!m.matches()) {
			doc.time = text;
			return;
		}
		try {
			int startYear = Integer.parseInt(m.group(1));
			Calendar c = Calendar.getInstance();
			c.set(startYear, Calendar.JANUARY, 1, 0, 0);
			doc.validFrom = c.getTime();
			int endYear = Integer.parseInt(m.group(2));
			c.set(endYear, Calendar.DECEMBER, 31, 0, 0);
			doc.validUntil = c.getTime();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to convert time", e);
		}
	}

	private void mapDescription() {
		StringBuilder builder = new StringBuilder();
		if (block.comment != null)
			builder.append(block.comment);
		a("Status", block.status, builder);
		a("Boundary with nature", block.boundaryWithNature, builder);
		a("Record", block.record, builder);
		a("Generator", block.generator, builder);
		process.description = builder.toString();
	}

	private void a(String label, String value, StringBuilder builder) {
		if (value == null)
			return;
		builder.append(label).append(": ").append(value).append("\n");
	}

	private String a(String label, String value, String field) {
		if (value == null)
			return field;
		return field == null
			? label + ": " + value + "\n"
			: field + label + ": " + value + "\n";
	}

	private String a(String label, ValueEnum val, String field) {
		if (val == null)
			return field;
		return field == null
			? label + ": " + val.getValue() + "\n"
			: field + label + ": " + val.getValue() + "\n";
	}

	private void a(String label, ValueEnum venum, StringBuilder builder) {
		if (venum == null)
			return;
		builder.append(label).append(": ").append(venum.getValue())
				.append("\n");
	}

}
