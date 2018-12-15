package org.openlca.io.xls.results.system;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.results.ContributionResult;
import org.openlca.io.xls.results.CellWriter;

class ProcessFlowContributionSheet
		extends ContributionSheet<CategorizedDescriptor, FlowDescriptor> {

	private final CellWriter writer;
	private final ContributionResult result;

	static void write(ResultExport export, ContributionResult result) {
		new ProcessFlowContributionSheet(export, result)
				.write(export.workbook, export.processes, export.flows);
	}

	private ProcessFlowContributionSheet(ResultExport export,
			ContributionResult result) {
		super(export.writer, ResultExport.PROCESS_HEADER,
				ResultExport.FLOW_HEADER);
		this.writer = export.writer;
		this.result = result;
	}

	private void write(Workbook workbook, List<CategorizedDescriptor> processes,
			List<FlowDescriptor> flows) {
		Sheet sheet = workbook.createSheet("Process flow contributions");
		header(sheet);
		subHeaders(sheet, processes, flows);
		data(sheet, processes, flows);
	}

	@Override
	protected double getValue(CategorizedDescriptor process,
			FlowDescriptor flow) {
		return result.getDirectFlowResult(process, flow);
	}

	@Override
	protected void subHeaderCol(CategorizedDescriptor process, Sheet sheet,
			int col) {
		writer.processCol(sheet, 1, col, process);
	}

	@Override
	protected void subHeaderRow(FlowDescriptor flow, Sheet sheet, int row) {
		writer.flowRow(sheet, row, 1, flow);
	}

}
