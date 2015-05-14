package org.openlca.io.xls.results;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.ContributionItem;
import org.openlca.core.results.ContributionResultProvider;
import org.openlca.core.results.ContributionSet;
import org.openlca.io.xls.Excel;
import org.openlca.io.xls.results.AnalysisResultExport.FlowVisitor;

/**
 * Writes the contributions of the flows to the overall impact assessment result
 * into an Excel sheet.
 */
class FlowImpacts {

	private Sheet sheet;
	private ContributionResultProvider<?> result;
	private AnalysisResultExport export;

	private int startRow;
	private int startCol;

	private FlowImpacts(Sheet sheet, ContributionResultProvider<?> result,
			AnalysisResultExport export) {
		this.sheet = sheet;
		this.result = result;
		this.export = export;
		startRow = CellWriter.IMPACT_INFO_SIZE + 1;
		startCol = CellWriter.FLOW_INFO_SIZE;
	}

	public static void write(Sheet sheet, ContributionResultProvider<?> result,
			AnalysisResultExport export) {
		new FlowImpacts(sheet, result, export).doIt();
	}

	private void doIt() {
		export.getWriter().writeImpactColHeader(sheet, startCol);
		export.getWriter().writeFlowRowHeader(sheet, startRow);
		export.visitFlows(new FlowInfoWriter());
		FlowValueWriter valueWriter = new FlowValueWriter();
		int col = startCol + 1;
		for (ImpactCategoryDescriptor impact : export.getImpacts()) {
			export.getWriter().writeImpactColInfo(sheet, col, impact);
			ContributionSet<FlowDescriptor> contributions = result
					.getFlowContributions(impact);
			valueWriter.setNext(contributions, col);
			export.visitFlows(valueWriter);
			col++;
		}
		// there are problems with auto-size when the sheet is streamed
		// Excel.autoSize(sheet, 1, 2, 3, 4, 5, 6);
	}

	private class FlowInfoWriter implements FlowVisitor {

		private int row = startRow + 1;

		@Override
		public void next(FlowDescriptor flow, boolean input) {
			export.getWriter().writeFlowRowInfo(sheet, row++, flow);
		}
	}

	private class FlowValueWriter implements FlowVisitor {

		private int row;
		private int col;
		private ContributionSet<FlowDescriptor> set;

		private void setNext(ContributionSet<FlowDescriptor> set, int col) {
			this.set = set;
			row = startRow + 1;
			this.col = col;
		}

		public void next(FlowDescriptor flow, boolean input) {
			ContributionItem<FlowDescriptor> c = set.getContribution(flow);
			if (c == null)
				return;
			Excel.cell(sheet, row++, col, c.getAmount());
		}
	}

}
