package org.openlca.io.xls.process;

import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.doc.ProcessDoc;

class OutDocSync {

	private final OutConfig config;
	private final Process process;
	private final ProcessDoc doc;

	private OutDocSync(OutConfig config) {
		this.config = config;
		this.process = config.process();
		this.doc = process.documentation == null
			? new ProcessDoc()
			: process.documentation;
	}

	static void sync(OutConfig config) {
		new OutDocSync(config).sync();
	}

	private void sync() {
		var sheet = config.createSheet(Tab.DOCUMENTATION)
			.withColumnWidths(2, 40);

		sheet.next(Section.LCI_METHOD)
			.next(Field.PROCESS_TYPE, process.processType == ProcessType.LCI_RESULT
				? "LCI result"
				: "Unit process")
			.next(Field.LCI_METHOD, doc.inventoryMethod)
			.next(Field.MODELING_CONSTANTS, doc.modelingConstants)
			.next();

		sheet.next(Section.DATA_SOURCE_INFO)
			.next(Field.DATA_COMPLETENESS, doc.dataCompleteness)
			.next(Field.DATA_SELECTION, doc.dataSelection)
			.next(Field.DATA_TREATMENT, doc.dataTreatment)
			.next(Field.SAMPLING_PROCEDURE, doc.samplingProcedure)
			.next(Field.DATA_COLLECTION_PERIOD, doc.dataCollectionPeriod)
			.next(Field.USE_ADVICE, doc.useAdvice)
			.next();

		// flow completeness
		sheet.next(Section.COMPLETENESS);
		doc.flowCompleteness.each(
			(key, val) -> sheet.next(row -> row.next(key).next(val)));
		sheet.next();

		sheet.next(Section.SOURCES);
		for (var source : doc.sources) {
			sheet.next(source);
		}

		sheet.next()
			.next(Section.ADMINISTRATIVE_INFO)
			.next(Field.PROJECT, doc.project)
			.next(Field.INTENDED_APPLICATION, doc.intendedApplication)
			.next(Field.DATA_SET_OWNER, doc.dataOwner)
			.next(Field.DATA_GENERATOR, doc.dataGenerator)
			.next(Field.DATA_DOCUMENTOR, doc.dataDocumentor)
			.next(Field.PUBLICATION, doc.publication)
			.next(Field.CREATION_DATE, doc.creationDate)
			.next(Field.COPYRIGHT, doc.copyright)
			.next(Field.ACCESS_RESTRICTIONS, doc.accessRestrictions);
	}

}
