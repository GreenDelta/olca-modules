package org.openlca.io.xls.process;

import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.doc.ProcessDoc;

class InInfoSync {

	private final InConfig config;
	private final Process process;
	private final ProcessDoc doc;

	private InInfoSync(InConfig config) {
		this.config = config;
		this.process = config.process();
		if (process.documentation == null) {
			process.documentation = new ProcessDoc();
		}
		this.doc = process.documentation;
	}

	static void sync(InConfig config) {
		new InInfoSync(config).sync();
	}

	private void sync() {
		var sheet = config.getSheet(Tab.GENERAL_INFO);
		if (sheet == null)
			return;

		var info = sheet.read(Section.GENERAL_INFO);
		if (info != null) {
			process.category = info.syncCategory(config.db(), ModelType.PROCESS);
			process.description = info.str(Field.DESCRIPTION);
			process.tags = info.str(Field.TAGS);
		} else {
			process.category = null;
			process.description = null;
			process.tags = null;
		}

		var time = sheet.read(Section.TIME);
		if (time != null) {
			doc.validFrom = time.date(Field.VALID_FROM);
			doc.validUntil = time.date(Field.VALID_UNTIL);
			doc.time = time.str(Field.DESCRIPTION);
		} else {
			doc.validFrom = null;
			doc.validUntil = null;
			doc.time = null;
		}

		var geo = sheet.read(Section.GEOGRAPHY);
		if (geo != null) {
			process.location = geo.get(Field.LOCATION, config, Location.class);
			doc.geography = geo.str(Field.DESCRIPTION);
		} else {
			process.location = null;
			doc.geography = null;
		}

		var dqs = sheet.read(Section.DATA_QUALITY);
		if (dqs != null) {
			process.dqSystem = dqs.get(
				Field.PROCESS_SCHEMA, config, DQSystem.class);
			process.exchangeDqSystem = dqs.get(
				Field.FLOW_SCHEMA, config, DQSystem.class);
			process.socialDqSystem = dqs.get(
				Field.SOCIAL_SCHEMA, config, DQSystem.class);
			process.dqEntry = dqs.str(Field.DATA_QUALITY_ENTRY);
		} else {
			process.dqSystem = null;
			process.dqEntry = null;
			process.exchangeDqSystem = null;
			process.socialDqSystem = null;
		}
	}
}
