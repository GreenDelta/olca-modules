package org.openlca.io.xls.results.system;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.math.data_quality.DQResult;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.results.SimpleResult;
import org.openlca.io.xls.results.CellWriter;

class InventorySheet {

	private final CellWriter writer;
	private final Workbook workbook;
	private final SimpleResult result;
	private final DQResult dqResult;
	private final List<FlowDescriptor> flows;
	private Sheet sheet;

	static void write(ResultExport export) {
		new InventorySheet(export).write();
	}

	private InventorySheet(ResultExport export) {
		this.writer = export.writer;
		this.workbook = export.workbook;
		this.result = export.result;
		this.dqResult = export.dqResult;
		this.flows = export.flows;
	}

	private void write() {
		sheet = workbook.createSheet("Inventory");
		int col = header(1, true);
		header(col, false);
		data(1, filterByInputType(true));
		data(col, filterByInputType(false));
	}

	private List<FlowDescriptor> filterByInputType(boolean input) {
		List<FlowDescriptor> filtered = new ArrayList<>();
		for (FlowDescriptor flow : flows) {
			if (result.isInput(flow) != input)
				continue;
			filtered.add(flow);
		}
		return filtered;
	}

	private int header(int col, boolean input) {
		writer.headerRow(sheet, 1, col, input ? "Inputs" : "Outputs");
		int row = 2;
		col = writer.headerRow(sheet, row, col, ResultExport.FLOW_HEADER);
		writer.cell(sheet, row, col++, "Result", true);
		if (!withDQ())
			return col + 1;
		col = writer.dataQualityHeader(sheet, row, col,
				dqResult.setup.exchangeDqSystem);
		return col + 1;
	}

	private void data(int col, List<FlowDescriptor> flows) {
		int row = 3;
		int startCol = ResultExport.FLOW_HEADER.length;
		for (FlowDescriptor flow : flows) {
			double value = result.getTotalFlowResult(flow);
			writer.flowRow(sheet, row, col, flow);
			writer.cell(sheet, row, startCol + col, value);
			if (!withDQ()) {
				row++;
				continue;
			}
			writer.dataQuality(sheet, row++, startCol + col + 1,
					dqResult.get(flow),
					dqResult.setup.exchangeDqSystem);
		}
	}

	private boolean withDQ() {
		return dqResult != null
				&& dqResult.setup != null
				&& dqResult.setup.exchangeDqSystem != null;
	}

}
