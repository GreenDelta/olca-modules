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

	public static void read(final Config config) {
		new InfoSheet(config).read();
	}

	private final Config config;
	private final ProcessDocumentation doc;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Process process;

	private final Sheet sheet;

	private InfoSheet(final Config config) {
		this.config = config;
		this.process = config.process;
		this.doc = config.process.getDocumentation();
		sheet = config.workbook.getSheet("General information");
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
			doc.setTechnology(config.getString(sheet, 21, 1));
		} catch (final Exception e) {
			log.error("failed to read information sheet", e);
		}
	}

	private void readGeography() {
		final String code = config.getString(sheet, 17, 1);
		if (code != null) {
			process.setLocation(config.refData.getLocation(code));
		}
		doc.setGeography(config.getString(sheet, 18, 1));
	}

	private void readInfoSection() {
		process.setRefId(config.getString(sheet, 1, 1));
		process.setName(config.getString(sheet, 2, 1));
		process.setDescription(config.getString(sheet, 3, 1));
		final String categoryPath = config.getString(sheet, 4, 1);
		process.setCategory(config.getCategory(categoryPath, ModelType.PROCESS));
		final String version = config.getString(sheet, 5, 1);
		process.setVersion(Version.fromString(version).getValue());
		final Date lastChange = config.getDate(sheet, 6, 1);
		if (lastChange == null) {
			process.setLastChange(0L);
		} else {
			process.setLastChange(lastChange.getTime());
		}
	}

	private void readQuanRef() {
		// the outputs must be already imported
		final String qRefName = config.getString(sheet, 9, 1);
		Exchange qRef = null;
		for (final Exchange exchange : process.getExchanges()) {
			if (exchange.isInput() || exchange.getFlow() == null) {
				continue;
			}
			if (Objects.equals(qRefName, exchange.getFlow().getName())) {
				qRef = exchange;
				break;
			}
		}
		if (qRef == null) {
			log.warn("could not find quantitative reference {}", qRefName);
		} else {
			process.setQuantitativeReference(qRef);
		}
	}

	private void readTime() {
		doc.setValidFrom(config.getDate(sheet, 12, 1));
		doc.setValidUntil(config.getDate(sheet, 13, 1));
		doc.setTime(config.getString(sheet, 14, 1));
	}

}
