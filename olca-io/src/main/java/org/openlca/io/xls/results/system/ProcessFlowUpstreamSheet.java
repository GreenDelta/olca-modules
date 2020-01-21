package org.openlca.io.xls.results.system;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.results.FullResult;
import org.openlca.io.xls.results.CellWriter;

class ProcessFlowUpstreamSheet
		extends ContributionSheet<CategorizedDescriptor, IndexFlow> {

	private final CellWriter writer;
	private final FullResult result;

	static void write(ResultExport export, FullResult result) {
		new ProcessFlowUpstreamSheet(export, result)
				.write(export.workbook, export.processes, export.flows);
	}

	private ProcessFlowUpstreamSheet(ResultExport export, FullResult result) {
		super(export.writer, ResultExport.PROCESS_HEADER,
				ResultExport.FLOW_HEADER);
		this.writer = export.writer;
		this.result = result;
	}

	private void write(Workbook workbook, List<CategorizedDescriptor> processes,
			List<IndexFlow> flows) {
		Sheet sheet = workbook.createSheet("Process upstream flows");
		header(sheet);
		subHeaders(sheet, processes, flows);
		data(sheet, processes, flows);
	}

	@Override
	protected double getValue(CategorizedDescriptor process, IndexFlow flow) {
		return result.getUpstreamFlowResult(process, flow);
	}

	@Override
	protected void subHeaderCol(CategorizedDescriptor process, Sheet sheet,
			int col) {
		writer.processCol(sheet, 1, col, process);
	}

	@Override
	protected void subHeaderRow(IndexFlow flow, Sheet sheet, int row) {
		writer.flowRow(sheet, row, 1, flow);
	}
}
