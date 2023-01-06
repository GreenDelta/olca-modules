package org.openlca.io.xls.process.output;

class AdminInfoSheet {

	private final ProcessWorkbook wb;

	AdminInfoSheet(ProcessWorkbook wb) {
		this.wb = wb;
	}

	void write() {
		var doc = wb.process.documentation;
		var cursor = wb.createCursor("Administrative information");
		cursor.header("Administrative information")
				.next("Intended application", doc.intendedApplication)
				.next("Data set owner", doc.dataSetOwner)
				.next("Data set generator", doc.dataGenerator)
				.next("Data set documentor", doc.dataDocumentor)
				.next("Publication", doc.publication)
				.next("Access and use restrictions", doc.restrictions)
				.next("Project", doc.project)
				.next("Creation date", doc.creationDate)
				.next("Copyright", doc.copyright);
	}
}
