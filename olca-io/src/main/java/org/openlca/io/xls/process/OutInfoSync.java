package org.openlca.io.xls.process;

import java.util.Date;

import org.openlca.core.model.Process;
import org.openlca.core.model.Version;
import org.openlca.core.model.doc.ProcessDoc;

class OutInfoSync {

	private final OutConfig config;
	private final Process process;
	private final ProcessDoc doc;

	private OutInfoSync(OutConfig config) {
		this.config = config;
		this.process = config.process();
		this.doc = process.documentation == null
			? new ProcessDoc()
			: process.documentation;
	}

	static void sync(OutConfig config) {
		new OutInfoSync(config).sync();
	}

	private void sync() {
		var sheet = config.createSheet(Tab.GENERAL_INFO)
			.withColumnWidths(2, 40);

		sheet.next(Section.GENERAL_INFO)
			.next(Field.UUID, process.refId)
			.next(Field.NAME, process.name)
			.next(Field.CATEGORY, Out.pathOf(process))
			.next(Field.DESCRIPTION, process.description)
			.next(Field.VERSION, Version.asString(process.version))
			.next(Field.LAST_CHANGE, process.lastChange > 0
				? new Date(process.lastChange)
				: null)
			.next(Field.TAGS, process.tags)
			.next();

		sheet.next(Section.TIME)
			.next(Field.VALID_FROM, doc.validFrom)
			.next(Field.VALID_UNTIL, doc.validUntil)
			.next(Field.DESCRIPTION, doc.time)
			.next();

		sheet.next(Section.GEOGRAPHY)
			.next(Field.LOCATION, process.location)
			.next(Field.DESCRIPTION, doc.geography)
			.next();

		sheet.next(Section.TECHNOLOGY)
			.next(Field.DESCRIPTION, doc.technology)
			.next();

		sheet.next(Section.DATA_QUALITY)
			.next(Field.PROCESS_SCHEMA, process.dqSystem)
			.next(Field.DATA_QUALITY_ENTRY, process.dqEntry)
			.next(Field.FLOW_SCHEMA, process.exchangeDqSystem)
			.next(Field.SOCIAL_SCHEMA, process.socialDqSystem)
			.next();
	}
}
