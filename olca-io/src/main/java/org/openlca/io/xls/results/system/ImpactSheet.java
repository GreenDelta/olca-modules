package org.openlca.io.xls.results.system;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.math.data_quality.DQResult;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.SimpleResult;
import org.openlca.io.xls.results.CellWriter;

class ImpactSheet {

	private final CellWriter writer;
	private final Workbook workbook;
	private final SimpleResult result;
	private final DQResult dqResult;
	private final NwSet nwSet;
	private Sheet sheet;

	static void write(ResultExport export) {
		new ImpactSheet(export).write();
	}

	private ImpactSheet(ResultExport export) {
		this.writer = export.writer;
		this.workbook = export.workbook;
		this.result = export.result;
		this.dqResult = export.dqResult;
		this.nwSet = export.nwSet;
	}

	private void write() {
		sheet = workbook.createSheet("Impacts");
		header();
		data();
	}

	private void header() {
		int row = 1;
		int col = writer.headerRow(sheet, row, 1, ResultExport.IMPACT_HEADER);
		writer.headerRow(sheet, row, col++, "Result");
		if (nwSet != null) {
			col = writer.headerRow(sheet, row, col, ResultExport.IMPACT_NW_HEADER);
		}
		if (dqResult == null || dqResult.setup.exchangeSystem == null)
			return;
		writer.dataQualityHeader(sheet, row, col, dqResult.setup.exchangeSystem);
	}

	private void data() {
		int row = 2;
		for (ImpactDescriptor impact : result.getImpacts()) {
			double value = result.getTotalImpactResult(impact);
			int col = writer.impactRow(sheet, row, 1, impact);
			writer.cell(sheet, row, col++, value);
			if (nwSet != null) {
				col = writer.impactNwRow(sheet, row, col, impact, value, nwSet);
			}
			if (dqResult == null || dqResult.setup == null) {
				row++;
				continue;
			}
			writer.dataQuality(sheet, row++, col + 1,
					dqResult.get(impact),
					dqResult.setup.exchangeSystem);
		}
	}

}
