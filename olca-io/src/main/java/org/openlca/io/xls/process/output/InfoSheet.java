package org.openlca.io.xls.process.output;

import java.util.Date;

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
		var cursor = wb.createCursor("General information")
				.withColumnWidths(2, 40);
		var doc = wb.process.documentation;

		cursor.header("General information")
				.next("UUID", process.refId)
				.next("Name", process.name)
				.next("Category", CategoryPath.getFull(process.category))
				.next("Description", process.description)
				.next("Version", Version.asString(process.version))
				.next("Last change", process.lastChange > 0
						? new Date(process.lastChange)
						: null)
				.next("Tags", process.tags)
				.next();

		cursor.header("Time")
				.next("Valid from", doc.validFrom)
				.next("Valid until", doc.validUntil)
				.next("Description", doc.time)
				.next();

		cursor.header("Geography")
				.next("Location", process.location)
				.next("Description", doc.geography)
				.next();

		cursor.header("Technology")
				.next("Description", doc.technology)
				.next();

		cursor.header("Data quality")
				.next("Process schema", process.dqSystem)
				.next("Data quality entry", process.dqEntry)
				.next("Flow schema", process.exchangeDqSystem)
				.next("Social schema", process.socialDqSystem)
				.next();
	}

}
