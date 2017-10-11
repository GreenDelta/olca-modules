package org.openlca.io.xls.process.output;

import java.util.Date;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Version;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

class InfoSheet {

	private Config config;
	private Process process;
	private ProcessDocumentation doc;
	private Sheet sheet;
	private int row = 0;

	private InfoSheet(Config config) {
		this.config = config;
		process = config.process;
		doc = config.process.getDocumentation();
		sheet = config.workbook.createSheet("General information");
	}

	public static void write(Config config) {
		new InfoSheet(config).write();
	}

	private void write() {
		infoSection();
		row++;
		qRefSection();
		row++;
		timeSection();
		row++;
		geoSection();
		row++;
		techSection();
		Excel.autoSize(sheet, 0);
		sheet.setColumnWidth(1, 100 * 256);
	}

	private void infoSection() {
		config.header(sheet, row++, 0, "General information");
		writePair("UUID", process.getRefId());
		writePair("Name", process.getName());
		writePair("Description", process.getDescription());
		writePair("Category", CategoryPath.getFull(process.getCategory()));
		writePair("Version", Version.asString(process.getVersion()));
		Excel.cell(sheet, row, 0, "Last change");
		config.date(sheet, row++, 1, process.getLastChange());
	}

	private void qRefSection() {
		config.header(sheet, row++, 0, "Quantitative reference");
		String qRefName = null;
		Exchange qRef = process.getQuantitativeReference();
		if (qRef != null && qRef.flow != null)
			qRefName = qRef.flow.getName();
		writePair("Quantitative reference", qRefName);
	}

	private void timeSection() {
		config.header(sheet, row++, 0, "Time");
		writePair("Valid from", doc.getValidFrom());
		writePair("Valid until", doc.getValidUntil());
		writePair("Description", doc.getTime());
	}

	private void geoSection() {
		config.header(sheet, row++, 0, "Geography");
		Excel.cell(sheet, row, 0, "Location");
		Location loc = process.getLocation();
		if (loc != null)
			Excel.cell(sheet, row, 1, loc.getCode());
		row++;
		writePair("Description", doc.getGeography());
	}

	private void techSection() {
		config.header(sheet, row++, 0, "Technology");
		writePair("Description", doc.getTechnology());
	}

	private void writePair(String header, String value) {
		config.pair(sheet, row++, header, value);
	}

	private void writePair(String header, Date value) {
		Excel.cell(sheet, row, 0, header);
		config.date(sheet, row++, 1, value);
	}
}
