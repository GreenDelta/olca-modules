package org.openlca.io.xls.results;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.AnalysisResult;
import org.openlca.core.results.ContributionItem;
import org.openlca.core.results.ContributionSet;
import org.openlca.core.results.FlowImpactContribution;
import org.openlca.io.xls.Excel;
import org.openlca.io.xls.results.AnalysisResultExport.FlowVisitor;

/**
 * Writes the contributions of the flows to the overall impact assessment result
 * into an Excel sheet.
 */
class AnalysisFlowImpacts {

	private Sheet sheet;
	private AnalysisResult result;
	private AnalysisResultExport export;

	private int startRow;
	private int startCol;

	private AnalysisFlowImpacts(Sheet sheet, AnalysisResult result,
			AnalysisResultExport export) {
		this.sheet = sheet;
		this.result = result;
		this.export = export;
		startRow = CellWriter.IMPACT_INFO_SIZE + 1;
		startCol = CellWriter.FLOW_INFO_SIZE;
	}

	public static void write(Sheet sheet, AnalysisResult result,
			AnalysisResultExport export) {
		new AnalysisFlowImpacts(sheet, result, export).doIt();
	}

	private void doIt() {
		export.getWriter().writeImpactColHeader(sheet, startCol);
		export.getWriter().writeFlowRowHeader(sheet, startRow);
		export.visitFlows(new FlowInfoWriter());
		FlowImpactContribution contribution = new FlowImpactContribution(
				result, export.getCache());
		FlowValueWriter valueWriter = new FlowValueWriter();
		int col = startCol + 1;
		for (ImpactCategoryDescriptor impact : export.getImpacts()) {
			export.getWriter().writeImpactColInfo(sheet, col, impact);
			ContributionSet<FlowDescriptor> contributions = contribution
					.calculate(impact);
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
		private ContributionSet<FlowDescriptor> contributions;

		private void setNext(ContributionSet<FlowDescriptor> contributions,
				int col) {
			this.contributions = contributions;
			row = startRow + 1;
			this.col = col;
		}

		public void next(FlowDescriptor flow, boolean input) {
			ContributionItem<FlowDescriptor> contribution = contributions
					.getContribution(flow);
			if (contribution == null)
				return;
			Excel.cell(sheet, row++, col, contribution.getAmount());
		}
	}

}
