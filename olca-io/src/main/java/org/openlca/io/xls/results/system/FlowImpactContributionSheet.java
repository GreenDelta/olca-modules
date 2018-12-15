package org.openlca.io.xls.results.system;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.ContributionResult;
import org.openlca.io.xls.results.CellWriter;

class FlowImpactContributionSheet
		extends ContributionSheet<FlowDescriptor, ImpactCategoryDescriptor> {

	private final CellWriter writer;
	private final ContributionResult result;

	static void write(ResultExport export,
			ContributionResult result) {
		new FlowImpactContributionSheet(export, result)
				.write(export.workbook, export.flows, export.impacts);
	}

	private FlowImpactContributionSheet(ResultExport export,
			ContributionResult result) {
		super(export.writer, ResultExport.FLOW_HEADER,
				ResultExport.IMPACT_HEADER);
		this.writer = export.writer;
		this.result = result;
	}

	private void write(Workbook workbook, List<FlowDescriptor> flows,
			List<ImpactCategoryDescriptor> impacts) {
		Sheet sheet = workbook.createSheet("Flow impact contributions");
		header(sheet);
		subHeaders(sheet, flows, impacts);
		data(sheet, flows, impacts);
	}

	@Override
	protected double getValue(FlowDescriptor flow,
			ImpactCategoryDescriptor impact) {
		return result.getDirectFlowImpact(flow, impact);
	}

	@Override
	protected void subHeaderCol(FlowDescriptor flow, Sheet sheet, int col) {
		writer.flowCol(sheet, 1, col, flow);
	}

	@Override
	protected void subHeaderRow(ImpactCategoryDescriptor impact, Sheet sheet,
			int row) {
		writer.impactRow(sheet, row, 1, impact);
	}

}
