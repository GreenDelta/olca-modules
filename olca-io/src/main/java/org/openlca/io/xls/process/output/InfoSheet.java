package org.openlca.io.xls.process.output;

import org.openlca.core.model.Process;
import org.openlca.core.model.Version;
import org.openlca.io.CategoryPath;

class InfoSheet {

	private final ProcessWorkbook wb;
	private final Process process;

	InfoSheet(ProcessWorkbook wb) {
		this.wb = wb;
		this.process = wb.process;
	}

	void write() {
		var cursor = wb.createCursor("General information");
		var doc = wb.process.documentation;

		cursor.header("General information")
				.pair("UUID", process.refId)
				.pair("Name", process.name)
				.pair("Category", CategoryPath.getFull(process.category))
				.pair("Description", process.description)
				.pair("Version", Version.asString(process.version))
				.pairDate("Last change", process.lastChange)
				.pair("Tags", process.tags)
				.empty();

		cursor.header("Time")
				.pair("Valid from", doc.validFrom)
				.pair("Valid until", doc.validUntil)
				.pair("Description", doc.time)
				.empty();

		cursor.header("Geography")
				.pair("Location", process.location)
				.pair("Description", doc.geography)
				.empty();

		cursor.header("Technology")
				.pair("Description", doc.technology)
				.empty();

		cursor.header("Data quality")
				.pair("Process schema", process.dqSystem)
				.pair("Data quality entry", process.dqEntry)
				.pair("Flow schema", process.exchangeDqSystem)
				.pair("Social schema", process.socialDqSystem)
				.empty();
	}

}
