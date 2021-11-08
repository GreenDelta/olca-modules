package org.openlca.io.simapro.csv.input;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.openlca.simapro.csv.process.ProcessBlock;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

record ProcessDocs (RefData refData, ProcessBlock block,Process process )  {

	static void map(RefData refData, ProcessBlock block,Process process) {
		new ProcessDocs(refData, block, process).exec();
	}

	private void exec() {
		process.description = Text.of(block.comment())
			.join("Status", block.status())
			.join("Boundary with nature", block.boundaryWithNature())
			.join("Record", block.record())
			.join("Generator", block.generator())
			.value();
		if (process.documentation == null) {
			process.documentation = new ProcessDocumentation();
		}
		mapSources();
		mapDocFields();
		if (block.infrastructure() != null) {
			process.infrastructureProcess = block.infrastructure();
		}
	}

	private void mapSources() {
		var doc = process.documentation;
		for (var row : block.literatures()) {
			Source source = refData.sourceOf(row.name());
			if (source == null)
				continue;
			doc.sources.add(source);
		}
	}

	private void mapDocFields() {
		var doc = process.documentation;
		mapTime(doc);
		doc.geography = block.geography();
		doc.technology = block.technology();
		doc.dataSelection = block.representativeness();
		doc.dataTreatment = block.dataTreatment();
		doc.sampling = block.collectionMethod();
		doc.reviewDetails = block.verification();

		doc.inventoryMethod = Text.of("Allocation rules", block.allocationRules())
			.join("Multiple output allocation", block.allocation())
			.join("Substitution allocation", block.substitution())
			.value();

		doc.completeness = Text.of("Cut off rules", block.cutoff())
			.join("Capital goods", block.capitalGoods())
			.value();

		if (block.systemDescription() != null) {
			doc.project = Text.of("System", block.systemDescription().name())
				.join("Comment", block.systemDescription().comment())
				.value();
		}

		// TODO: parse date
		// doc.creationDate = block.date();
	}

	private void mapTime(ProcessDocumentation doc) {
		if (block.time() == null)
			return;
		String text = block.time();
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

	private record Text(String value) {

		static Text of(String value) {
			return Strings.nullOrEmpty(value)
				? new Text(null)
				: new Text(value.trim() + "\n");
		}

		static Text of(String header, String value) {
			if (Strings.nullOrEmpty(value))
				return new Text(null);
			return new Text(header + ": " + value + "\n");
		}

		Text join(String header, String value) {
			if (Strings.nullOrEmpty(value))
				return this;
			if (this.value == null)
				return of(header, value);
			return new Text(this.value + "\n" + header + ": " + value);
		}
	}
}
