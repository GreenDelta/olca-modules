package org.openlca.io.xls.process;

import org.openlca.commons.Strings;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.core.model.doc.ProcessDoc;

class InDocSync {

	private final InConfig config;
	private final Process process;
	private final ProcessDoc doc;

	private InDocSync(InConfig config) {
		this.config = config;
		this.process = config.process();
		if (process.documentation == null) {
			process.documentation = new ProcessDoc();
		}
		this.doc = process.documentation;
	}

	static void sync(InConfig config) {
		new InDocSync(config).sync();
	}

	private void sync() {
		var sheet = config.getSheet(Tab.DOCUMENTATION);
		if (sheet == null)
			return;

		// LCI method
		var inventory = sheet.read(Section.LCI_METHOD);
		var type = inventory.str(Field.PROCESS_TYPE);
		process.processType =
			type != null && type.strip().equalsIgnoreCase("LCI result")
				? ProcessType.LCI_RESULT
				: ProcessType.UNIT_PROCESS;
		doc.inventoryMethod = inventory.str(Field.LCI_METHOD);
		doc.modelingConstants = inventory.str(Field.MODELING_CONSTANTS);

		// data source information
		var data = sheet.read(Section.DATA_SOURCE_INFO);
		doc.dataCompleteness = data.str(Field.DATA_COMPLETENESS);
		doc.dataSelection = data.str(Field.DATA_SELECTION);
		doc.dataTreatment = data.str(Field.DATA_TREATMENT);
		doc.samplingProcedure = data.str(Field.SAMPLING_PROCEDURE);
		doc.dataCollectionPeriod = data.str(Field.DATA_COLLECTION_PERIOD);
		doc.useAdvice = data.str(Field.USE_ADVICE);

		// flow completeness
		doc.flowCompleteness.clear();
		sheet.eachRowObject(Section.COMPLETENESS, row -> {
			var key = In.stringOf(In.cell(row, 0));
			var val = In.stringOf(In.cell(row, 1));
			if (Strings.isBlank(key) || Strings.isBlank(val))
				return;
			doc.flowCompleteness.put(key, val);
		});

		// sources
		doc.sources.clear();
		sheet.eachRowObject(Section.SOURCES, row -> {
			var name = In.stringOf(In.cell(row, 0));
			var source = config.index().get(Source.class, name);
			if (source != null) {
				doc.sources.add(source);
			}
		});

		// administrative information
		var section = sheet.read(Section.ADMINISTRATIVE_INFO);
		doc.project = section.str(Field.PROJECT);
		doc.intendedApplication = section.str(Field.INTENDED_APPLICATION);
		doc.dataOwner = section.get(Field.DATA_SET_OWNER, config, Actor.class);
		doc.dataGenerator = section.get(Field.DATA_GENERATOR, config, Actor.class);
		doc.dataDocumentor = section.get(Field.DATA_DOCUMENTOR, config, Actor.class);
		doc.publication = section.get(Field.PUBLICATION, config, Source.class);
		doc.creationDate = section.date(Field.CREATION_DATE);
		doc.copyright = section.bool(Field.COPYRIGHT);
		doc.accessRestrictions = section.str(Field.ACCESS_RESTRICTIONS);
	}
}
