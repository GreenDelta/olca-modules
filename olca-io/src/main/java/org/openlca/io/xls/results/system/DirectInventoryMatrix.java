package org.openlca.io.xls.results.system;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.ResultItemOrder;
import org.openlca.io.xls.results.CellWriter;

class DirectInventoryMatrix extends ContributionMatrix<TechFlow, EnviFlow> {

	private final CellWriter writer;
	private final LcaResult r;
	private final ResultItemOrder items;

	static void write(ResultExport export, LcaResult r) {
		new DirectInventoryMatrix(export, r).write(export.workbook);
	}

	private DirectInventoryMatrix(ResultExport export, LcaResult result) {
		super(export, ResultExport.PROCESS_HEADER, ResultExport.FLOW_HEADER);
		this.writer = export.writer;
		this.r = result;
		this.items = export.items();
	}

	private void write(Workbook workbook) {
		var sheet = workbook.createSheet("Direct inventory contributions");
		header(sheet);
		subHeaders(sheet, items.techFlows(), items.enviFlows());
		data(sheet, items.techFlows(), items.enviFlows());
	}

	@Override
	protected double getValue(TechFlow techFlow, EnviFlow flow) {
		return r.getDirectFlowOf(flow, techFlow);
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
