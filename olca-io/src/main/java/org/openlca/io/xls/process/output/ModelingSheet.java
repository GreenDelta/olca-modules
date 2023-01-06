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
				.next("Process type", wb.process.processType == ProcessType.LCI_RESULT
						? "LCI result"
						: "Unit process")
				.next("LCI method", doc.inventoryMethod)
				.next("Modeling constants", doc.modelingConstants)
				.next("Data completeness", doc.completeness)
				.next("Data selection", doc.dataSelection)
				.next("Data treatment", doc.dataTreatment)
				.next();

		cursor.header("Data source information")
				.next("Sampling procedure", doc.sampling)
				.next("Data collection period", doc.dataCollectionPeriod)
				.next();

		cursor.header("Process evaluation and validation")
				.next("Reviewer", doc.reviewer)
				.next("Review details", doc.reviewDetails)
				.next();

		cursor.header("Sources");
		for (var source : doc.sources) {
			cursor.next(source);
		}
	}
}
