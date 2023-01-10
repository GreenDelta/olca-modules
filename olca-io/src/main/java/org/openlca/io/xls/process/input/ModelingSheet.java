package org.openlca.io.xls.process.input;

import java.util.Objects;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.util.Strings;

class ModelingSheet {

	private final ProcessWorkbook wb;
	private final ProcessDocumentation doc;
	private final Process process;
	private final Sheet sheet;

	private ModelingSheet(ProcessWorkbook wb) {
		this.wb = wb;
		process = wb.process;
		doc = wb.process.documentation;
		sheet = wb.getSheet("Modeling and validation");
	}

	public static void read(ProcessWorkbook wb) {
		new ModelingSheet(wb).read();
	}

	private void read() {
		if (sheet == null) {
			return;
		}
		readModelingSection();
		readDataSourceSection();
		readReviewSection();
		readSources();
	}

	private void readModelingSection() {
		String type = wb.getString(sheet, 1, 1);
		if (Objects.equals(type, "LCI result")) {
			process.processType = ProcessType.LCI_RESULT;
		} else {
			process.processType = ProcessType.UNIT_PROCESS;
		}
		doc.inventoryMethod = wb.getString(sheet, 2, 1);
		doc.modelingConstants = wb.getString(sheet, 3, 1);
		doc.completeness = wb.getString(sheet, 4, 1);
		doc.dataSelection = wb.getString(sheet, 5, 1);
		doc.dataTreatment = wb.getString(sheet, 6, 1);
	}

	private void readDataSourceSection() {
		doc.sampling = wb.getString(sheet, 9, 1);
		doc.dataCollectionPeriod = wb.getString(sheet, 10, 1);
	}

	private void readReviewSection() {
		var reviewer = wb.getString(sheet, 13, 1);
		doc.reviewer = Strings.notEmpty(reviewer)
				? wb.index.get(Actor.class, reviewer)
				: null;
		doc.reviewDetails = wb.getString(sheet, 14, 1);
	}

	private void readSources() {
		int row = 17;
		while (true) {
			String name = wb.getString(sheet, row, 0);
			if (Strings.nullOrEmpty(name))
				break;
			var source = wb.index.get(Source.class, name);
			if (source != null) {
				doc.sources.add(source);
			}
			row++;
		}
	}
}
