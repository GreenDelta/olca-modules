package org.openlca.io.xls.results.system;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.ResultItemOrder;
import org.openlca.io.xls.results.CellWriter;

class FlowImpactMatrix extends ContributionMatrix<EnviFlow, ImpactDescriptor> {

	private final CellWriter writer;
	private final LcaResult r;
	private final ResultItemOrder items;

	static void write(ResultExport export, LcaResult result) {
		new FlowImpactMatrix(export, result)
				.write(export.workbook);
	}

	private FlowImpactMatrix(ResultExport export, LcaResult result) {
		super(export, ResultExport.FLOW_HEADER, ResultExport.IMPACT_HEADER);
		this.writer = export.writer;
		this.r = result;
		this.items = export.items();
	}

	private void write(Workbook wb) {
		var sheet = wb.createSheet("Impact contributions by flow");
		header(sheet);
		subHeaders(sheet, items.enviFlows(), items.impacts());
		data(sheet, items.enviFlows(), items.impacts());
	}

	@Override
	protected double getValue(EnviFlow flow, ImpactDescriptor impact) {
		return r.getFlowImpactOf(impact, flow);
	}

	@Override
	protected void subHeaderCol(EnviFlow flow, Sheet sheet, int col) {
		writer.flowCol(sheet, 1, col, flow);
	}

	@Override
	protected void subHeaderRow(ImpactDescriptor impact, Sheet sheet, int row) {
		writer.impactRow(sheet, row, 1, impact);
	}
}
