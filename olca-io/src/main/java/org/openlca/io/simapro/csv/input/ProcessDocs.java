package org.openlca.io.simapro.csv.input;

import org.openlca.core.model.Process;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.core.model.Source;
import org.openlca.core.model.doc.Review;
import org.openlca.simapro.csv.process.ProcessBlock;
import org.openlca.util.Strings;

record ProcessDocs(RefData refData, ProcessBlock block, Process process) {

	static void map(RefData refData, ProcessBlock block, Process process) {
		new ProcessDocs(refData, block, process).exec();
	}

	private void exec() {
		process.description = Text.of(block.comment())
			.join("Status", block.status() != null
				? block.status().toString()
				: null)
			.join("Record", block.record())
			.join("Generator", block.generator())
			.value();
		if (process.documentation == null) {
			process.documentation = new ProcessDoc();
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
		doc.dataTreatment = block.dataTreatment();
		doc.samplingProcedure = block.collectionMethod();
		doc.inventoryMethod = block.allocationRules();
		doc.creationDate = block.date();
		if (block.systemDescription() != null) {
			doc.project = Text.of("System", block.systemDescription().name())
				.join("Comment", block.systemDescription().comment())
				.value();
		}
		if (Strings.notEmpty(block.verification())) {
			var r = new Review();
			r.details = block.verification();
			doc.reviews.add(r);
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

		@Override
		public String value() {
			return value != null
				? Strings.cut(value, 64 * 1024)
				: null;
		}
	}
}
