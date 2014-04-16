package org.openlca.io.xls.process.output;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

class ModelingSheet {

	private ProcessDocumentation doc;
	private Config config;
	private Sheet sheet;
	private int row = 0;

	private ModelingSheet(Config config) {
		this.config = config;
		doc = config.process.getDocumentation();
		sheet = config.workbook.createSheet("Modeling and validation");
	}

	public static void write(Config config) {
		new ModelingSheet(config).write();
	}

	private void write() {
		writeModelingSection();
		row++;
		writeDataSourceSection();
		row++;
		writeReviewSection();
		row++;
		writeSources();
		Excel.autoSize(sheet, 0);
		sheet.setColumnWidth(1, 100 * 256);
	}

	private void writeModelingSection() {
		config.header(sheet, row++, 0, "Modeling and validation");
		String type = config.process.getProcessType() == ProcessType.LCI_RESULT ?
				"LCI result" : "Unit process";
		pair("Process type", type);
		pair("LCI method", doc.getInventoryMethod());
		pair("Modeling constants", doc.getModelingConstants());
		pair("Data completeness", doc.getCompleteness());
		pair("Data selection", doc.getDataSelection());
		pair("Data treatment", doc.getDataTreatment());
	}

	private void writeDataSourceSection() {
		config.header(sheet, row++, 0, "Data source information");
		pair("Sampling procedure", doc.getSampling());
		pair("Data collection period", doc.getDataCollectionPeriod());
	}

	private void writeReviewSection() {
		config.header(sheet, row++, 0, "Process evaluation and validation");
		pair("Reviewer", doc.getReviewer());
		pair("Review details", doc.getReviewDetails());
	}

	private void writeSources() {
		config.header(sheet, row++, 0, "Sources");
		for (Source source : doc.getSources()) {
			Excel.cell(sheet, row, 0, source.getName());
			Excel.cell(sheet, row++, 1, CategoryPath.getFull(source.getCategory()));
		}
	}

	private void pair(String header, CategorizedEntity entity) {
		if (entity == null) {
			Excel.cell(sheet, row++, 0, header);
			return;
		}
		Excel.cell(sheet, row, 0, header);
		Excel.cell(sheet, row, 1, entity.getName());
		Excel.cell(sheet, row++, 2, CategoryPath.getFull(entity.getCategory()));
	}

	private void pair(String header, String value) {
		config.pair(sheet, row++, header, value);
	}
}
