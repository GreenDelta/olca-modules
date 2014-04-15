package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Version;

import java.util.Date;

class InfoSheet {

	private Config config;
	private Process process;
	private ProcessDocumentation doc;
	private Sheet sheet;

	private InfoSheet(Config config) {
		this.config = config;
		this.process = config.process;
		this.doc = config.process.getDocumentation();
		sheet = config.workbook.getSheet("General information");
	}

	public static void read(Config config) {
		new InfoSheet(config).read();
	}

	private void read() {
		if (sheet == null)
			return;
		readInfoSection();
	}

	private void readInfoSection() {
		process.setRefId(config.getString(sheet, 1, 1));
		process.setName(config.getString(sheet, 2, 1));
		process.setDescription(config.getString(sheet, 3, 1));
		String categoryPath = config.getString(sheet, 4, 1);
		process.setCategory(config.getCategory(categoryPath, ModelType.PROCESS));
		String version = config.getString(sheet, 5, 1);
		process.setVersion(Version.fromString(version).getValue());
		Date lastChange = config.getDate(sheet, 6, 1);
		if (lastChange == null)
			process.setLastChange(0L);
		else
			process.setLastChange(lastChange.getTime());
	}

}
