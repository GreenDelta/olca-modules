package org.openlca.io.xls.results.system;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.ContributionResult;
import org.openlca.io.xls.results.CellWriter;

class ProcessImpactContributionSheet
		extends
		ContributionSheet<CategorizedDescriptor, ImpactCategoryDescriptor> {

	private final CellWriter writer;
	private final ContributionResult result;

	static void write(ResultExport export, ContributionResult result) {
		new ProcessImpactContributionSheet(export, result)
				.write(export.workbook, export.processes, export.impacts);
	}

	private ProcessImpactContributionSheet(ResultExport export,
			ContributionResult result) {
		super(export.writer, ResultExport.PROCESS_HEADER,
				ResultExport.IMPACT_HEADER);
		this.writer = export.writer;
		this.result = result;
	}

	private void write(Workbook workbook, List<CategorizedDescriptor> processes,
			List<ImpactCategoryDescriptor> impacts) {
		Sheet sheet = workbook.createSheet("Process impact contributions");
		header(sheet);
		subHeaders(sheet, processes, impacts);
		data(sheet, processes, impacts);
	}

	@Override
	protected double getValue(CategorizedDescriptor process,
			ImpactCategoryDescriptor impact) {
		return result.getDirectImpactResult(process, impact);
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
