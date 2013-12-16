package org.openlca.io.xls.results;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.AnalysisResult;
import org.openlca.io.xls.Excel;

/**
 * Writes the total impact assessment result of an analysis result to an Excel
 * sheet. The total impact assessment result is the upstream total result of the
 * reference process.
 */
class AnalysisTotalImpact {

	private Sheet sheet;
	private AnalysisResult result;
	private AnalysisResultExport export;

	private AnalysisTotalImpact(Sheet sheet, AnalysisResult result,
			AnalysisResultExport export) {
		this.sheet = sheet;
		this.result = result;
		this.export = export;
	}

	public static void write(Sheet sheet, AnalysisResult result,
			AnalysisResultExport export) {
		new AnalysisTotalImpact(sheet, result, export).doIt();
	}

	private void doIt() {
		int col = CellWriter.IMPACT_INFO_SIZE + 1;
		export.getWriter().writeImpactRowHeader(sheet, 1);
		export.getWriter().header(sheet, 1, col, "Result");
		int row = 2;
		long refProcess = result.getProductIndex().getRefProduct().getFirst();
		for (ImpactCategoryDescriptor impact : export.getImpacts()) {
			export.getWriter().writeImpactRowInfo(sheet, row, impact);
			double val = result
					.getTotalImpactResult(refProcess, impact.getId());
			Excel.cell(sheet, row, col, val);
			row++;
		}
		// there are problems with auto-size when the sheet is streamed
		// Excel.autoSize(sheet, 1, 2, 3, 4);
	}

}
