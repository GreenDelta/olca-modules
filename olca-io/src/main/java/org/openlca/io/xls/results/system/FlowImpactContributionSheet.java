package org.openlca.io.xls.results.system;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.ContributionResult;
import org.openlca.io.xls.results.CellWriter;

class FlowImpactContributionSheet
		extends ContributionSheet<EnviFlow, ImpactDescriptor> {

	private final CellWriter writer;
	private final ContributionResult r;

	static void write(ResultExport export,
			ContributionResult result) {
		new FlowImpactContributionSheet(export, result)
				.write(export.workbook);
	}

	private FlowImpactContributionSheet(ResultExport export,
			ContributionResult result) {
		super(export.writer, ResultExport.FLOW_HEADER,
				ResultExport.IMPACT_HEADER);
		this.writer = export.writer;
		this.r = result;
	}

	private void write(Workbook wb) {
		Sheet sheet = wb.createSheet("Flow impact contributions");
		header(sheet);
		subHeaders(sheet, r.getFlows(), r.getImpacts());
		data(sheet, r.getFlows(), r.getImpacts());
	}

	@Override
	protected double getValue(EnviFlow flow, ImpactDescriptor impact) {
		return r.getDirectFlowImpact(flow, impact);
	}

	@Override
	protected void subHeaderCol(EnviFlow flow, Sheet sheet, int col) {
		writer.flowCol(sheet, 1, col, flow);
	}

	@Override
	protected void subHeaderRow(ImpactDescriptor impact, Sheet sheet,
			int row) {
		writer.impactRow(sheet, row, 1, impact);
	}

}
