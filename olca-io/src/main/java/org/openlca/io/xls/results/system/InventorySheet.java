package org.openlca.io.xls.results.system;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.math.data_quality.DQResult;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.results.SimpleResult;
import org.openlca.io.xls.results.CellWriter;

class InventorySheet {

	private final CellWriter writer;
	private final Workbook workbook;
	private final SimpleResult result;
	private final DQResult dqResult;
	private Sheet sheet;

	static void write(ResultExport export) {
		new InventorySheet(export).write();
	}

	private InventorySheet(ResultExport export) {
		this.writer = export.writer;
		this.workbook = export.workbook;
		this.result = export.result;
		this.dqResult = export.dqResult;
	}

	private void write() {
		sheet = workbook.createSheet("Inventory");
		int col = header(1, true);
		header(col, false);
		data(1, filterByInputType(true));
		data(col, filterByInputType(false));
	}

	private List<EnviFlow> filterByInputType(boolean input) {
		return result.getFlows().stream()
				.filter(f -> f.isInput() == input)
				.collect(Collectors.toList());
	}

	private int header(int col, boolean input) {
		writer.headerRow(sheet, 1, col, input ? "Inputs" : "Outputs");
		int row = 2;
		col = writer.headerRow(sheet, row, col, ResultExport.FLOW_HEADER);
		writer.cell(sheet, row, col++, "Result", true);
		if (!withDQ())
			return col + 1;
		col = writer.dataQualityHeader(sheet, row, col,
				dqResult.setup.exchangeSystem);
		return col + 1;
	}

	private void data(int col, List<EnviFlow> flows) {
		int row = 3;
		int startCol = ResultExport.FLOW_HEADER.length;
		for (EnviFlow flow : flows) {
			double value = result.getTotalFlowResult(flow);
			writer.flowRow(sheet, row, col, flow);
			writer.cell(sheet, row, startCol + col, value);
			if (!withDQ()) {
				row++;
				continue;
			}
			writer.dataQuality(sheet, row++, startCol + col + 1,
					dqResult.get(flow),
					dqResult.setup.exchangeSystem);
		}
	}

	private boolean withDQ() {
		return dqResult != null
				&& dqResult.setup != null
				&& dqResult.setup.exchangeSystem != null;
	}

}
