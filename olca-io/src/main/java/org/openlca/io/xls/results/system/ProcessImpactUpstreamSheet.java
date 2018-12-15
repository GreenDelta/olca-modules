package org.openlca.io.xls.results.system;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.FullResult;
import org.openlca.io.xls.results.CellWriter;

class ProcessImpactUpstreamSheet extends
		ContributionSheet<CategorizedDescriptor, ImpactCategoryDescriptor> {

	private final CellWriter writer;
	private final FullResult result;

	static void write(ResultExport export, FullResult result) {
		new ProcessImpactUpstreamSheet(export, result)
				.write(export.workbook, export.processes, export.impacts);
	}

	private ProcessImpactUpstreamSheet(ResultExport export, FullResult result) {
		super(export.writer, ResultExport.PROCESS_HEADER,
				ResultExport.FLOW_HEADER);
		this.writer = export.writer;
		this.result = result;
	}

	private void write(Workbook workbook, List<CategorizedDescriptor> processes,
			List<ImpactCategoryDescriptor> impacts) {
		Sheet sheet = workbook.createSheet("Process upstream impacts");
		header(sheet);
		subHeaders(sheet, processes, impacts);
		data(sheet, processes, impacts);
	}

	@Override
	protected double getValue(CategorizedDescriptor process,
			ImpactCategoryDescriptor impact) {
		return result.getUpstreamImpactResult(process, impact);
	}

	@Override
	protected void subHeaderCol(CategorizedDescriptor process, Sheet sheet,
			int col) {
		writer.processCol(sheet, 1, col, process);
	}

	@Override
	protected void subHeaderRow(ImpactCategoryDescriptor impact, Sheet sheet,
			int row) {
		writer.impactRow(sheet, row, 1, impact);
	}
}
