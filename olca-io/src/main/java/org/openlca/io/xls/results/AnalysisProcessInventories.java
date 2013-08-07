package org.openlca.io.xls.results;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.AnalysisResult;
import org.openlca.io.xls.Excel;
import org.openlca.io.xls.results.AnalysisResultExport.FlowVisitor;

/**
 * Writes the single process inventories of an analysis result ( = single
 * contributions of processes to the overall inventory result) to an Excel
 * sheet. The export format is a matrix where the flows are listed in the rows
 * and the processes with their contributions in the columns.
 */
class AnalysisProcessInventories {

	private Sheet sheet;
	private AnalysisResult result;
	private AnalysisResultExport export;

	private int inputStartRow;
	private int firstValCol;
	private int outputStartRow;

	private AnalysisProcessInventories(Sheet sheet, AnalysisResult result,
			AnalysisResultExport export) {
		this.sheet = sheet;
		this.result = result;
		this.export = export;
		inputStartRow = export.PROCESS_INFO_SIZE + 2;
		firstValCol = export.FLOW_INFO_SIZE + 1;
	}

	public static void write(Sheet sheet, AnalysisResult result,
			AnalysisResultExport export) {
		new AnalysisProcessInventories(sheet, result, export).doIt();
	}

	private void doIt() {
		export.writeProcessColHeader(sheet, export.FLOW_INFO_SIZE);

		// inputs
		Excel.cell(sheet, export.PROCESS_INFO_SIZE, 1, "Inputs").setCellStyle(
				export.getHeaderStyle());
		export.writeFlowRowHeader(sheet, export.PROCESS_INFO_SIZE + 1);
		FlowInfoWriter inputInfoWriter = new FlowInfoWriter(true, inputStartRow);
		export.visitFlows(inputInfoWriter);
		int nextRow = inputInfoWriter.currentRow + 1;

		// outputs
		Excel.cell(sheet, nextRow++, 1, "Outputs").setCellStyle(
				export.getHeaderStyle());
		export.writeFlowRowHeader(sheet, nextRow++);
		outputStartRow = nextRow;
		FlowInfoWriter outputInfoWriter = new FlowInfoWriter(false,
				outputStartRow);
		export.visitFlows(outputInfoWriter);

		writeValues();
		// Excel.autoSize(sheet, 1, 2, 3, 4, 5, 6, 7);
	}

	private void writeValues() {
		int col = firstValCol;
		ValueWriter inputWriter = new ValueWriter(true, inputStartRow);
		ValueWriter outputWriter = new ValueWriter(false, outputStartRow);
		for (ProcessDescriptor p : export.getProcesses()) {
			export.writeProcessColInfo(sheet, col, p);
			inputWriter.setProcess(p, col);
			outputWriter.setProcess(p, col);
			export.visitFlows(inputWriter);
			export.visitFlows(outputWriter);
			col++;
		}
	}

	private class FlowInfoWriter implements FlowVisitor {

		private boolean forInputs;
		private int currentRow;

		public FlowInfoWriter(boolean forInputs, int startRow) {
			this.forInputs = forInputs;
			this.currentRow = startRow;
		}

		@Override
		public void next(FlowDescriptor flow, boolean input) {
			if (input != forInputs)
				return;
			export.writeFlowRowInfo(sheet, currentRow, flow);
			currentRow++;
		}
	}

	private class ValueWriter implements FlowVisitor {

		private boolean forInputs;
		private int currentRow;
		private int startRow;
		private ProcessDescriptor process;
		private int column;

		public ValueWriter(boolean forInputs, int startRow) {
			this.forInputs = forInputs;
			this.startRow = startRow;
			this.currentRow = startRow;
		}

		public void setProcess(ProcessDescriptor process, int column) {
			this.process = process;
			this.column = column;
			this.currentRow = startRow;
		}

		@Override
		public void next(FlowDescriptor flow, boolean input) {
			if (forInputs != input)
				return;
			if (process == null || flow == null)
				return;
			double val = result.getSingleFlowResult(process.getId(),
					flow.getId());
			Excel.cell(sheet, currentRow, column, val);
			currentRow++;
		}
	}

}
