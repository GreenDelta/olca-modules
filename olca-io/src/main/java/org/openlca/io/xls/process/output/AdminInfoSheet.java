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
				.pair("Intended application", doc.intendedApplication)
				.pair("Data set owner", doc.dataSetOwner)
				.pair("Data set generator", doc.dataGenerator)
				.pair("Data set documentor", doc.dataDocumentor)
				.pair("Publication", doc.publication)
				.pair("Access and use restrictions", doc.restrictions)
				.pair("Project", doc.project)
				.pair("Creation date", doc.creationDate)
				.pair("Copyright", doc.copyright);
	}
}
