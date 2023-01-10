package org.openlca.io.xls.process.input;

import java.util.Date;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Version;
import org.openlca.util.Strings;

class InfoSheet {


	private final ProcessWorkbook wb;
	private final ProcessDocumentation doc;
	private final Process process;
	private final Sheet sheet;

	private InfoSheet(ProcessWorkbook wb) {
		this.wb = wb;
		this.process = wb.process;
		this.doc = wb.process.documentation;
		sheet = wb.getSheet("General information");
	}

	public static void read(ProcessWorkbook config) {
		new InfoSheet(config).read();
	}

	private void read() {
		if (sheet == null) {
			return;
		}
		readInfoSection();
		readQuanRef();
		readTime();
		readGeography();
		doc.technology = wb.getString(sheet, 21, 1);
	}

	private void readInfoSection() {
		process.refId = wb.getString(sheet, 1, 1);
		process.name = wb.getString(sheet, 2, 1);
		process.description = wb.getString(sheet, 3, 1);
		var categoryPath = wb.getString(sheet, 4, 1);
		process.category = wb.getCategory(categoryPath, ModelType.PROCESS);
		String version = wb.getString(sheet, 5, 1);
		process.version = Version.fromString(version).getValue();
		Date lastChange = wb.getDate(sheet, 6, 1);
		if (lastChange == null) {
			process.lastChange = 0L;
		} else {
			process.lastChange = lastChange.getTime();
		}
	}

	private void readQuanRef() {
		// the outputs must be already imported
		String qRefName = wb.getString(sheet, 9, 1);
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
			wb.log.warn("could not find quantitative reference " + qRefName);
		} else {
			process.quantitativeReference = qRef;
		}
	}

	private void readTime() {
		doc.validFrom = wb.getDate(sheet, 12, 1);
		doc.validUntil = wb.getDate(sheet, 13, 1);
		doc.time = wb.getString(sheet, 14, 1);
	}

	private void readGeography() {
		String code = wb.getString(sheet, 17, 1);
		if (Strings.notEmpty(code)) {
			process.location = wb.index.get(Location.class, code);
		}
		doc.geography = wb.getString(sheet, 18, 1);
	}

}
