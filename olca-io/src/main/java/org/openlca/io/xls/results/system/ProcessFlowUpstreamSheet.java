package org.openlca.io.xls.results.system;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.FullResultProvider;
import org.openlca.io.xls.results.CellWriter;

class ProcessFlowUpstreamSheet
		extends ContributionSheet<ProcessDescriptor, FlowDescriptor> {

	private final CellWriter writer;
	private final FullResultProvider result;

	static void write(ResultExport export,
			FullResultProvider result) {
		new ProcessFlowUpstreamSheet(export, result)
				.write(export.workbook, export.processes, export.flows);
	}

	private ProcessFlowUpstreamSheet(ResultExport export,
			FullResultProvider result) {
		super(export.writer, ResultExport.PROCESS_HEADER,
				ResultExport.FLOW_HEADER);
		this.writer = export.writer;
		this.result = result;
	}

	private void write(Workbook workbook, List<ProcessDescriptor> processes,
			List<FlowDescriptor> flows) {
		Sheet sheet = workbook.createSheet("Process upstream flows");
		header(sheet);
		subHeaders(sheet, processes, flows);
		data(sheet, processes, flows);
	}

	@Override
	protected double getValue(ProcessDescriptor process, FlowDescriptor flow) {
		return result.getUpstreamFlowResult(process, flow).value;
	}

	@Override
	protected void subHeaderCol(ProcessDescriptor process, Sheet sheet,
			int col) {
		writer.processCol(sheet, 1, col, process);
	}

	@Override
	protected void subHeaderRow(FlowDescriptor flow, Sheet sheet,
			int row) {
		writer.flowRow(sheet, row, 1, flow);
	}
}
