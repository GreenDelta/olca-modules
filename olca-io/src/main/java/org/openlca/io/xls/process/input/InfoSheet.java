package org.openlca.io.xls.process.input;

import java.util.Date;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InfoSheet {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final Config config;
	private final ProcessDocumentation doc;
	private final Process process;
	private final Sheet sheet;

	private InfoSheet(Config config) {
		this.config = config;
		this.process = config.process;
		this.doc = config.process.documentation;
		sheet = config.workbook.getSheet("General information");
	}

	public static void read(Config config) {
		new InfoSheet(config).read();
	}

	private void read() {
		if (sheet == null) {
			return;
		}
		try {
			log.trace("read information sheet");
			readInfoSection();
			readQuanRef();
			readTime();
			readGeography();
			doc.technology = config.getString(sheet, 21, 1);
		} catch (Exception e) {
			log.error("failed to read information sheet", e);
		}
	}

	private void readInfoSection() {
		process.refId = config.getString(sheet, 1, 1);
		process.name = config.getString(sheet, 2, 1);
		process.description = config.getString(sheet, 3, 1);
		String categoryPath = config.getString(sheet, 4, 1);
		process.category = config.getCategory(categoryPath, ModelType.PROCESS);
		String version = config.getString(sheet, 5, 1);
		process.version = Version.fromString(version).getValue();
		Date lastChange = config.getDate(sheet, 6, 1);
		if (lastChange == null) {
			process.lastChange = 0L;
		} else {
			process.lastChange = lastChange.getTime();
		}
	}

	private void readQuanRef() {
		// the outputs must be already imported
		String qRefName = config.getString(sheet, 9, 1);
		Exchange qRef = null;
		for (Exchange exchange : process.exchanges) {
			if (exchange.isInput || exchange.flow == null) {
				continue;
			}
			if (Objects.equals(qRefName, exchange.flow.name)) {
				qRef = exchange;
				break;
			}
		}
		if (qRef == null) {
			log.warn("could not find quantitative reference {}", qRefName);
		} else {
			process.quantitativeReference = qRef;
		}
	}

	private void readTime() {
		doc.validFrom = config.getDate(sheet, 12, 1);
		doc.validUntil = config.getDate(sheet, 13, 1);
		doc.time = config.getString(sheet, 14, 1);
	}

	private void readGeography() {
		String code = config.getString(sheet, 17, 1);
		if (code != null) {
			process.location = config.refData.getLocation(code);
		}
		doc.geography = config.getString(sheet, 18, 1);
	}

}
