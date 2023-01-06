package org.openlca.io.xls.process.output;

import org.openlca.core.model.ProcessType;

class ModelingSheet {

	private final ProcessWorkbook wb;

	ModelingSheet(ProcessWorkbook wb) {
		this.wb = wb;
	}

	void write() {
		var cursor = wb.createCursor("Modeling and validation");
		var doc = wb.process.documentation;

		cursor.header("Modeling and validation")
				.pair("Process type", wb.process.processType == ProcessType.LCI_RESULT
						? "LCI result"
						: "Unit process")
				.pair("LCI method", doc.inventoryMethod)
				.pair("Modeling constants", doc.modelingConstants)
				.pair("Data completeness", doc.completeness)
				.pair("Data selection", doc.dataSelection)
				.pair("Data treatment", doc.dataTreatment)
				.empty();

		cursor.header("Data source information")
				.pair("Sampling procedure", doc.sampling)
				.pair("Data collection period", doc.dataCollectionPeriod)
				.empty();

		cursor.header("Process evaluation and validation")
				.pair("Reviewer", doc.reviewer)
				.pair("Review details", doc.reviewDetails)
				.empty();

		cursor.header("Sources");
		for (var source : doc.sources) {
			cursor.next(source);
		}
	}
}
