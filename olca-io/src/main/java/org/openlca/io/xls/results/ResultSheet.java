package org.openlca.io.xls.results;

import java.math.RoundingMode;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.math.data_quality.DQResult;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.ContributionResultProvider;

class ResultSheet {

	private final CellWriter writer;
	private final Workbook workbook;
	private final ContributionResultProvider<?> result;
	private final DQResult dqResult;
	private final String[] header1;
	private final List<? extends BaseDescriptor> data1;
	private final String[] header2;
	private final List<? extends BaseDescriptor> data2;
	private Sheet sheet;

	static void write(ResultExport export, String title,
			String[] header1, List<? extends BaseDescriptor> data1,
			String[] header2, List<? extends BaseDescriptor> data2) {
		new ResultSheet(export, header1, data1, header2, data2).write(title);
	}

	private ResultSheet(ResultExport export,
			String[] header1, List<? extends BaseDescriptor> data1,
			String[] header2, List<? extends BaseDescriptor> data2) {
		this.writer = export.writer;
		this.workbook = export.workbook;
		this.result = export.result;
		this.dqResult = export.dqResult;
		this.header1 = header1;
		this.data1 = data1;
		this.header2 = header2;
		this.data2 = data2;
	}

	private void write(String title) {
		sheet = workbook.createSheet(title);
		header();
		data();
	}

	private void write(Sheet sheet, int row, int col, BaseDescriptor descriptor, boolean bold) {
		if (descriptor instanceof ProcessDescriptor) {
			writer.process(sheet, row, col, (ProcessDescriptor) descriptor, bold);
		} else if (descriptor instanceof ImpactCategoryDescriptor) {
			writer.impact(sheet, row, col, (ImpactCategoryDescriptor) descriptor, bold);
		} else if (descriptor instanceof FlowDescriptor) {
			writer.flow(sheet, row, col, (FlowDescriptor) descriptor, bold);
		}
	}

	private double getResult(BaseDescriptor descriptor) {
		if (descriptor instanceof ImpactCategoryDescriptor)
			return result.getTotalImpactResult((ImpactCategoryDescriptor) descriptor).value;
		if (descriptor instanceof FlowDescriptor)
			return result.getTotalFlowResult((FlowDescriptor) descriptor).value;
		return 0d;
	}

	private double getResult(BaseDescriptor d1, BaseDescriptor d2) {
		if (d1 instanceof FlowDescriptor && d2 instanceof ProcessDescriptor) {
			ProcessDescriptor process = (ProcessDescriptor) d2;
			FlowDescriptor flow = (FlowDescriptor) d1;
			return result.getSingleFlowResult(process, flow).value;
		} else if (d1 instanceof ImpactCategoryDescriptor && d2 instanceof ProcessDescriptor) {
			ProcessDescriptor process = (ProcessDescriptor) d2;
			ImpactCategoryDescriptor impact = (ImpactCategoryDescriptor) d1;
			return result.getSingleImpactResult(process, impact).value;
		} else if (d1 instanceof ImpactCategoryDescriptor && d2 instanceof FlowDescriptor) {
			FlowDescriptor flow = (FlowDescriptor) d2;
			ImpactCategoryDescriptor impact = (ImpactCategoryDescriptor) d1;
			return result.getFlowContributions(impact).getContribution(flow).amount;
		}
		return 0;
	}

	private double[] getQuality(BaseDescriptor descriptor) {
		if (dqResult == null || dqResult.setup.exchangeDqSystem == null)
			return null;
		if (descriptor instanceof ImpactCategoryDescriptor)
			return dqResult.get((ImpactCategoryDescriptor) descriptor);
		if (descriptor instanceof FlowDescriptor)
			return dqResult.get((FlowDescriptor) descriptor);
		return null;
	}

	private double[] getQuality(BaseDescriptor d1, BaseDescriptor d2) {
		if (dqResult == null || dqResult.setup.exchangeDqSystem == null)
			return null;
		if (d1 instanceof FlowDescriptor && d2 instanceof ProcessDescriptor) {
			ProcessDescriptor process = (ProcessDescriptor) d2;
			FlowDescriptor flow = (FlowDescriptor) d1;
			return dqResult.get(process, flow);
		} else if (d1 instanceof ImpactCategoryDescriptor && d2 instanceof ProcessDescriptor) {
			ProcessDescriptor process = (ProcessDescriptor) d2;
			ImpactCategoryDescriptor impact = (ImpactCategoryDescriptor) d1;
			return dqResult.get(process, impact);
		} else if (d1 instanceof ImpactCategoryDescriptor && d2 instanceof FlowDescriptor) {
			FlowDescriptor flow = (FlowDescriptor) d2;
			ImpactCategoryDescriptor impact = (ImpactCategoryDescriptor) d1;
			return dqResult.get(flow, impact);
		}
		return null;
	}

	private void header() {
		int col = writer.headerRow(sheet, 1, 1, header1);
		col = writer.headerRow(sheet, 1, col, header2);
		writer.cell(sheet, 1, col++, "Result", true);
		if (dqResult == null || dqResult.setup.exchangeDqSystem == null)
			return;
		writer.dataQualityHeader(sheet, 1, col, dqResult.setup.exchangeDqSystem);
	}

	private void data() {
		int row = 2;
		int resultStartCol = header1.length + header2.length + 1;
		int subStartCol = header1.length + 1;
		RoundingMode rounding = null;
		int scores = 0;
		if (dqResult != null && dqResult.setup.exchangeDqSystem != null) {
			rounding = dqResult.setup.roundingMode;
			scores = dqResult.setup.exchangeDqSystem.getScoreCount();
		}
		for (BaseDescriptor d1 : data1) {
			write(sheet, row, 1, d1, true);
			writer.cell(sheet, row, resultStartCol, Double.toString(getResult(d1)), true);
			writer.dataQuality(sheet, row, resultStartCol + 1, getQuality(d1), rounding, scores, true);
			row++;
			for (BaseDescriptor d2 : data2) {
				write(sheet, row, subStartCol, d2, false);
				writer.cell(sheet, row, resultStartCol, Double.toString(getResult(d1, d2)));
				writer.dataQuality(sheet, row, resultStartCol + 1, getQuality(d1, d2), rounding, scores, false);
				row++;
			}
		}
	}

}
