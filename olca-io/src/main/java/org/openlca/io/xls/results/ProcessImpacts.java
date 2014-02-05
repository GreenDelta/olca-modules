package org.openlca.io.xls.results;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.ContributionResultProvider;
import org.openlca.io.xls.Excel;

/**
 * Writes the single process impact assessment contributions to an Excel sheet.
 * The export format is a matrix where the processes are listed in the rows and
 * the impact assessment categories in the columns.
 */
class ProcessImpacts {

	private Sheet sheet;
	private ContributionResultProvider<?> result;
	private AnalysisResultExport export;

	private int startRow;
	private int startCol;

	private ProcessImpacts(Sheet sheet, ContributionResultProvider<?> result,
	                       AnalysisResultExport export) {
		this.sheet = sheet;
		this.result = result;
		this.export = export;
		startRow = CellWriter.IMPACT_INFO_SIZE + 1;
		startCol = CellWriter.PROCESS_INFO_SIZE;
	}

	public static void write(Sheet sheet, ContributionResultProvider<?> result,
	                         AnalysisResultExport export) {
		new ProcessImpacts(sheet, result, export).doIt();
	}

	private void doIt() {
		export.getWriter().writeImpactColHeader(sheet, startCol);
		export.getWriter().writeProcessRowHeader(sheet, startRow);
		int col = startCol + 1;
		for (ImpactCategoryDescriptor impact : export.getImpacts()) {
			export.getWriter().writeImpactColInfo(sheet, col, impact);
			int row = startRow + 1;
			for (ProcessDescriptor process : export.getProcesses()) {
				export.getWriter().writeProcessRowInfo(sheet, row, process);
				double val = result.getSingleImpactResult(process,
						impact).getValue();
				Excel.cell(sheet, row, col, val);
				row++;
			}
			col++;
		}
		// there are problems with auto-size when the sheet is streamed
		// Excel.autoSize(sheet, 1, 2, 3, 4);
	}
}
