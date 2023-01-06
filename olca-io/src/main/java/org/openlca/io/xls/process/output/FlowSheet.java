package org.openlca.io.xls.process.output;

import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Flow;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

class FlowSheet implements EntitySheet {

	private final ProcessWorkbook wb;
	private final Set<Flow> flows = new HashSet<>();

	FlowSheet(ProcessWorkbook wb) {
		this.wb = wb;
	}

	@Override
	public void visit(RootEntity entity) {
		if (entity instanceof Flow flow) {
			flows.add(flow);
		}
	}

	@Override
	public void flush() {
		if (flows.isEmpty())
			return;
		var sheet = wb.createSheet("Flows");
		writeHeader(sheet);
		int row = 0;
		for (var flow : Util.sort(flows)) {
			row++;
			write(sheet, row, flow);
		}
	}

	private void writeHeader(Sheet sheet) {
		wb.header(sheet, 0, 0, "UUID");
		wb.header(sheet, 0, 1, "Name");
		wb.header(sheet, 0, 2, "Description");
		wb.header(sheet, 0, 3, "Category");
		wb.header(sheet, 0, 4, "Version");
		wb.header(sheet, 0, 5, "Last change");
		wb.header(sheet, 0, 6, "Type");
		wb.header(sheet, 0, 7, "CAS");
		wb.header(sheet, 0, 8, "Formula");
		wb.header(sheet, 0, 9, "Location");
		wb.header(sheet, 0, 10, "Reference flow property");
	}

	private void write(Sheet sheet, int row, Flow flow) {
		Excel.cell(sheet, row, 0, flow.refId);
		Excel.cell(sheet, row, 1, flow.name);
		Excel.cell(sheet, row, 2, flow.description);
		Excel.cell(sheet, row, 3, CategoryPath.getFull(flow.category));
		Excel.cell(sheet, row, 4, Version.asString(flow.version));
		wb.date(sheet, row, 5, flow.lastChange);
		Excel.cell(sheet, row, 6, getType(flow));
		Excel.cell(sheet, row, 7, flow.casNumber);
		Excel.cell(sheet, row, 8, flow.formula);
		if (flow.location != null) {
			Excel.cell(sheet, row, 9, flow.location.name);
		}
		if (flow.referenceFlowProperty != null) {
			Excel.cell(sheet, row, 10, flow.referenceFlowProperty.name);
		}
	}

	private String getType(Flow flow) {
		if (flow.flowType == null)
			return "Elementary flow";
		return switch (flow.flowType) {
			case PRODUCT_FLOW -> "Product flow";
			case WASTE_FLOW -> "Waste flow";
			default -> "Elementary flow";
		};
	}
}
