package org.openlca.io.xls.results.system;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.ResultItemOrder;
import org.openlca.io.xls.results.CellWriter;

class ProcessImpactUpstreamSheet extends
		ContributionSheet<TechFlow, ImpactDescriptor> {

	private final CellWriter writer;
	private final LcaResult r;
	private final ResultItemOrder items;

	static void write(ResultExport export, LcaResult r) {
		new ProcessImpactUpstreamSheet(export, r).write(export.workbook);
	}

	private ProcessImpactUpstreamSheet(ResultExport export, LcaResult r) {
		super(export.writer, ResultExport.PROCESS_HEADER,
				ResultExport.FLOW_HEADER);
		this.writer = export.writer;
		this.r = r;
		this.items = export.items();
	}

	private void write(Workbook wb) {
		var sheet = wb.createSheet("Process upstream impacts");
		header(sheet);
		subHeaders(sheet, items.techFlows(), items.impacts());
		data(sheet, items.techFlows(), items.impacts());
	}

	@Override
	protected double getValue(TechFlow techFlow, ImpactDescriptor impact) {
		return r.getTotalImpactOf(impact, techFlow);
	}

	@Override
	protected void subHeaderCol(TechFlow techFlow, Sheet sheet, int col) {
		writer.processCol(sheet, 1, col, techFlow);
	}

	@Override
	protected void subHeaderRow(ImpactDescriptor impact, Sheet sheet, int row) {
		writer.impactRow(sheet, row, 1, impact);
	}
}
