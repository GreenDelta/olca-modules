package org.openlca.io.xls.results.system;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.results.FullResult;
import org.openlca.io.xls.results.CellWriter;

class ProcessFlowUpstreamSheet
		extends ContributionSheet<RootDescriptor, EnviFlow> {

	private final CellWriter writer;
	private final FullResult r;

	static void write(ResultExport export, FullResult r) {
		new ProcessFlowUpstreamSheet(export, r)
				.write(export.workbook);
	}

	private ProcessFlowUpstreamSheet(ResultExport export, FullResult r) {
		super(export.writer, ResultExport.PROCESS_HEADER,
				ResultExport.FLOW_HEADER);
		this.writer = export.writer;
		this.r = r;
	}

	private void write(Workbook workbook) {
		Sheet sheet = workbook.createSheet("Process upstream flows");
		header(sheet);
		subHeaders(sheet, r.getProcesses(), r.getFlows());
		data(sheet, r.getProcesses(), r.getFlows());
	}

	@Override
	protected double getValue(RootDescriptor process, EnviFlow flow) {
		return r.getUpstreamFlowResult(process, flow);
	}

	@Override
	protected void subHeaderCol(RootDescriptor process, Sheet sheet,
                                int col) {
		writer.processCol(sheet, 1, col, process);
	}

	@Override
	protected void subHeaderRow(EnviFlow flow, Sheet sheet, int row) {
		writer.flowRow(sheet, row, 1, flow);
	}
}
