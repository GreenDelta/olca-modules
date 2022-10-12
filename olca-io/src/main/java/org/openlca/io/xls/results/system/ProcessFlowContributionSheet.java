package org.openlca.io.xls.results.system;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.ResultItemOrder;
import org.openlca.io.xls.results.CellWriter;

class ProcessFlowContributionSheet
		extends ContributionSheet<TechFlow, EnviFlow> {

	private final CellWriter writer;
	private final LcaResult r;
	private final ResultItemOrder items;

	static void write(ResultExport export, LcaResult r) {
		new ProcessFlowContributionSheet(export, r).write(export.workbook);
	}

	private ProcessFlowContributionSheet(ResultExport export, LcaResult result) {
		super(export.writer, ResultExport.PROCESS_HEADER, ResultExport.FLOW_HEADER);
		this.writer = export.writer;
		this.r = result;
		this.items = export.items();
	}

	private void write(Workbook workbook) {
		Sheet sheet = workbook.createSheet("Process flow contributions");
		header(sheet);
		subHeaders(sheet, items.techFlows(), items.enviFlows());
		data(sheet, items.techFlows(), items.enviFlows());
	}

	@Override
	protected double getValue(TechFlow techFlow, EnviFlow flow) {
		return r.directFlowOf(flow, techFlow);
	}

	@Override
	protected void subHeaderCol(TechFlow techFlow, Sheet sheet, int col) {
		writer.processCol(sheet, 1, col, techFlow);
	}

	@Override
	protected void subHeaderRow(EnviFlow flow, Sheet sheet, int row) {
		writer.flowRow(sheet, row, 1, flow);
	}

}
