package org.openlca.io.xls.results.system;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.ContributionResult;
import org.openlca.io.xls.results.CellWriter;

class ProcessImpactContributionSheet
		extends
		ContributionSheet<RootDescriptor, ImpactDescriptor> {

	private final CellWriter writer;
	private final ContributionResult r;

	static void write(ResultExport export, ContributionResult r) {
		new ProcessImpactContributionSheet(export, r)
				.write(export.workbook);
	}

	private ProcessImpactContributionSheet(ResultExport export,
			ContributionResult r) {
		super(export.writer, ResultExport.PROCESS_HEADER,
				ResultExport.IMPACT_HEADER);
		this.writer = export.writer;
		this.r = r;
	}

	private void write(Workbook wb) {
		Sheet sheet = wb.createSheet("Process impact contributions");
		header(sheet);
		subHeaders(sheet, r.getProcesses(), r.getImpacts());
		data(sheet, r.getProcesses(), r.getImpacts());
	}

	@Override
	protected double getValue(RootDescriptor process,
                              ImpactDescriptor impact) {
		return r.getDirectImpactResult(process, impact);
	}

	@Override
	protected void subHeaderCol(RootDescriptor process, Sheet sheet,
                                int col) {
		writer.processCol(sheet, 1, col, process);
	}

	@Override
	protected void subHeaderRow(ImpactDescriptor impact, Sheet sheet,
			int row) {
		writer.impactRow(sheet, row, 1, impact);
	}

}
