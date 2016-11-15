package org.openlca.io.xls.results.system;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.ContributionResultProvider;
import org.openlca.io.xls.results.CellWriter;

class ProcessImpactContributionSheet extends ContributionSheet<ProcessDescriptor, ImpactCategoryDescriptor> {

	private final CellWriter writer;
	private final ContributionResultProvider<?> result;

	static void write(ResultExport export) {
		new ProcessImpactContributionSheet(export).write(export.workbook, export.processes, export.impacts);
	}

	private ProcessImpactContributionSheet(ResultExport export) {
		super(export.writer, ResultExport.PROCESS_HEADER, ResultExport.IMPACT_HEADER);
		this.writer = export.writer;
		this.result = export.result;
	}

	private void write(Workbook workbook, List<ProcessDescriptor> processes, List<ImpactCategoryDescriptor> impacts) {
		Sheet sheet = workbook.createSheet("Process impact contributions");
		header(sheet);
		subHeaders(sheet, processes, impacts);
		data(sheet, processes, impacts);
	}

	@Override
	protected double getValue(ProcessDescriptor process, ImpactCategoryDescriptor impact) {
		return result.getSingleImpactResult(process, impact).value;
	}

	@Override
	protected void subHeaderCol(ProcessDescriptor process, Sheet sheet, int col) {
		writer.processCol(sheet, 1, col, process);
	}

	@Override
	protected void subHeaderRow(ImpactCategoryDescriptor impact, Sheet sheet, int row) {
		writer.impactRow(sheet, row, 1, impact);
	}

}
