package org.openlca.io.xls.process.output;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

class ModelingSheet {

	private final ProcessDocumentation doc;
	private final ProcessWorkbook wb;
	private final Sheet sheet;

	private int row = 0;

	private ModelingSheet(ProcessWorkbook wb) {
		this.wb = wb;
		doc = wb.process.documentation;
		sheet = wb.workbook.createSheet("Modeling and validation");
	}

	public static void write(ProcessWorkbook config) {
		new ModelingSheet(config).write();
	}

	private void write() {
		Excel.trackSize(sheet, 0, 0);
		writeModelingSection();
		row++;
		writeDataSourceSection();
		row++;
		writeReviewSection();
		row++;
		writeSources();
		Excel.autoSize(sheet, 0, 0);
		sheet.setColumnWidth(1, 100 * 256);
	}

	private void writeModelingSection() {
		wb.header(sheet, row++, 0, "Modeling and validation");
		var type = wb.process.processType == ProcessType.LCI_RESULT
				? "LCI result"
				: "Unit process";
		pair("Process type", type);
		pair("LCI method", doc.inventoryMethod);
		pair("Modeling constants", doc.modelingConstants);
		pair("Data completeness", doc.completeness);
		pair("Data selection", doc.dataSelection);
		pair("Data treatment", doc.dataTreatment);
	}

	private void writeDataSourceSection() {
		wb.header(sheet, row++, 0, "Data source information");
		pair("Sampling procedure", doc.sampling);
		pair("Data collection period", doc.dataCollectionPeriod);
	}

	private void writeReviewSection() {
		wb.header(sheet, row++, 0, "Process evaluation and validation");
		pair("Reviewer", doc.reviewer);
		pair("Review details", doc.reviewDetails);
	}

	private void writeSources() {
		wb.header(sheet, row++, 0, "Sources");
		for (Source source : doc.sources) {
			Excel.cell(sheet, row, 0, source.name);
			Excel.cell(sheet, row++, 1, CategoryPath.getFull(source.category));
		}
	}

	private void pair(String header, RootEntity entity) {
		if (entity == null) {
			Excel.cell(sheet, row++, 0, header);
			return;
		}
		wb.visit(entity);
		Excel.cell(sheet, row, 0, header);
		Excel.cell(sheet, row, 1, entity.name);
		Excel.cell(sheet, row++, 2, CategoryPath.getFull(entity.category));
	}

	private void pair(String header, String value) {
		wb.pair(sheet, row++, header, value);
	}
}
