package org.openlca.io.xls.results.system;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.ResultItemOrder;
import org.openlca.io.xls.results.CellWriter;

class TotalInventoryMatrix extends ContributionMatrix<TechFlow, EnviFlow> {

	private final CellWriter writer;
	private final LcaResult r;
	private final ResultItemOrder items;

	static void write(ResultExport export, LcaResult r) {
		new TotalInventoryMatrix(export, r)
				.write(export.workbook);
	}

	private TotalInventoryMatrix(ResultExport export, LcaResult r) {
		super(export.writer, ResultExport.PROCESS_HEADER, ResultExport.FLOW_HEADER);
		this.writer = export.writer;
		this.r = r;
		this.items = export.items();
	}

	private void write(Workbook workbook) {
		Sheet sheet = workbook.createSheet("Total upstream inventories");
		header(sheet);
		subHeaders(sheet, items.techFlows(), items.enviFlows());
		data(sheet, items.techFlows(), items.enviFlows());
	}

	@Override
	protected double getValue(TechFlow process, EnviFlow flow) {
		return r.getTotalFlowOf(flow, process);
	}

	@Override
	protected void subHeaderCol(TechFlow process, Sheet sheet, int col) {
		writer.processCol(sheet, 1, col, process);
	}

	@Override
	protected void subHeaderRow(EnviFlow flow, Sheet sheet, int row) {
		writer.flowRow(sheet, row, 1, flow);
	}
}
