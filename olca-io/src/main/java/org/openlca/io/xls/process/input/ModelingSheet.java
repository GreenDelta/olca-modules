package org.openlca.io.xls.process.input;

import java.util.Objects;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

class ModelingSheet {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final Config config;
	private final ProcessDocumentation doc;
	private final Process process;
	private final Sheet sheet;

	private ModelingSheet(Config config) {
		this.config = config;
		process = config.process;
		doc = config.process.getDocumentation();
		sheet = config.workbook.getSheet("Modeling and validation");
	}

	public static void read(Config config) {
		new ModelingSheet(config).read();
	}

	private void read() {
		if (sheet == null) {
			return;
		}
		try {
			log.trace("read modeling and validation");
			readModelingSection();
			readDataSourceSection();
			readReviewSection();
			readSources();
		} catch (Exception e) {
			log.error("failed to read modeling and validation", e);
		}
	}

	private void readModelingSection() {
		String type = config.getString(sheet, 1, 1);
		if (Objects.equals(type, "LCI result")) {
			process.setProcessType(ProcessType.LCI_RESULT);
		} else {
			process.setProcessType(ProcessType.UNIT_PROCESS);
		}
		doc.setInventoryMethod(config.getString(sheet, 2, 1));
		doc.setModelingConstants(config.getString(sheet, 3, 1));
		doc.setCompleteness(config.getString(sheet, 4, 1));
		doc.setDataSelection(config.getString(sheet, 5, 1));
		doc.setDataTreatment(config.getString(sheet, 6, 1));
	}

	private void readDataSourceSection() {
		doc.setSampling(config.getString(sheet, 9, 1));
		doc.setDataCollectionPeriod(config.getString(sheet, 10, 1));
	}

	private void readReviewSection() {
		String reviewer = config.getString(sheet, 13, 1);
		if (reviewer != null) {
			String category = config.getString(sheet, 13, 2);
			doc.setReviewer(config.refData.getActor(reviewer, category));
		}
		doc.setReviewDetails(config.getString(sheet, 14, 1));
	}

	private void readSources() {
		int row = 17;
		while (true) {
			String name = config.getString(sheet, row, 0);
			if (Strings.isNullOrEmpty(name)) {
				break;
			}
			String category = config.getString(sheet, row, 1);
			Source source = config.refData.getSource(name, category);
			if (source != null) {
				doc.getSources().add(source);
			}
			row++;
		}
	}
}