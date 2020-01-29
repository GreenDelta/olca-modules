package org.openlca.io.xls.results.system;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.math.data_quality.DQResult;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.SimpleResult;
import org.openlca.io.xls.results.CellWriter;

class ImpactSheet {

	private final CellWriter writer;
	private final Workbook workbook;
	private final SimpleResult result;
	private final DQResult dqResult;
	private Sheet sheet;

	static void write(ResultExport export) {
		new ImpactSheet(export).write();
	}

	private ImpactSheet(ResultExport export) {
		this.writer = export.writer;
		this.workbook = export.workbook;
		this.result = export.result;
		this.dqResult = export.dqResult;
	}

	private void write() {
		sheet = workbook.createSheet("Impacts");
		header();
		data();
	}

	private void header() {
		int row = 1;
		int col = writer.headerRow(sheet, row, 1, ResultExport.IMPACT_HEADER);
		writer.cell(sheet, row, col++, "Result", true);
		if (dqResult == null || dqResult.setup.exchangeDqSystem == null)
			return;
		writer.dataQualityHeader(sheet, row, col,
				dqResult.setup.exchangeDqSystem);
	}

	private void data() {
		int row = 2;
		int startCol = ResultExport.IMPACT_HEADER.length + 1;
		for (ImpactCategoryDescriptor impact : result.getImpacts()) {
			double value = result.getTotalImpactResult(impact);
			writer.impactRow(sheet, row, 1, impact);
			writer.cell(sheet, row, startCol, value);
			if (dqResult == null || dqResult.setup == null) {
				row++;
				continue;
			}
			writer.dataQuality(sheet, row++, startCol + 1,
					dqResult.get(impact),
					dqResult.setup.exchangeDqSystem);
		}
	}

}
