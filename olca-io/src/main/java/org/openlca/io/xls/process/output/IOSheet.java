package org.openlca.io.xls.process.output;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;
import org.openlca.util.Strings;

class IOSheet {

	private final ProcessWorkbook wb;
	private final Sheet sheet;
	private final boolean forInputs;
	private int row = 0;

	private IOSheet(ProcessWorkbook wb, boolean forInputs) {
		this.wb = wb;
		this.forInputs = forInputs;
		sheet = wb.workbook.createSheet(forInputs ? "Inputs" : "Outputs");
	}

	public static void writeInputs(ProcessWorkbook config) {
		new IOSheet(config, true).write();
	}

	public static void writeOutputs(ProcessWorkbook config) {
		new IOSheet(config, false).write();
	}

	private void write() {
		Excel.trackSize(sheet, 0, 12);
		witeHeader();
		row++;
		for (Exchange exchange : getExchanges()) {
			if (exchange.flow == null)
				continue;
			write(exchange);
			row++;
		}
		Excel.autoSize(sheet, 0, 12);
	}

	private void witeHeader() {
		wb.header(sheet, row, 0, "Flow");
		wb.header(sheet, row, 1, "Category");
		wb.header(sheet, row, 2, "Flow property");
		wb.header(sheet, row, 3, "Unit");
		wb.header(sheet, row, 4, "Amount");
		wb.header(sheet, row, 5, "Formula");
		wb.header(sheet, row, 6, "Description");
		wb.header(sheet, row, 7, "Uncertainty");
		wb.header(sheet, row, 8, "(g)mean | mode");
		wb.header(sheet, row, 9, "SD | GSD");
		wb.header(sheet, row, 10, "Minimum");
		wb.header(sheet, row, 11, "Maximum");
		if (!forInputs)
			wb.header(sheet, row, 12, "Is avoided product?");
	}

	private void write(Exchange e) {
		if (e.flow == null)
			return;

		// visit dependencies
		wb.visit(e.flow);
		wb.visit(e.currency);
		wb.visit(e.location);

		Excel.cell(sheet, row, 0, e.flow.name);
		Excel.cell(sheet, row, 1, CategoryPath.getFull(e.flow.category));
		Excel.cell(sheet, row, 2, getFlowProperty(e));
		Excel.cell(sheet, row, 3, e.unit != null ? e.unit.name : null);
		Excel.cell(sheet, row, 4, e.amount);
		Excel.cell(sheet, row, 5, e.formula);
		Excel.cell(sheet, row, 6, e.description);
		Util.write(sheet, row, 7, e.uncertainty);
		if (!forInputs)
			Excel.cell(sheet, row, 12, e.isAvoided ? "Yes": "");
	}

	private String getFlowProperty(Exchange exchange) {
		FlowPropertyFactor factor = exchange.flowPropertyFactor;
		if (factor == null)
			return null;
		FlowProperty prop = factor.flowProperty;
		return prop == null ? null : prop.name;
	}

	private List<Exchange> getExchanges() {
		var exchanges = new ArrayList<Exchange>();
		for (Exchange exchange : wb.process.exchanges) {
			if (exchange.isInput == forInputs)
				exchanges.add(exchange);
		}
		exchanges.sort((e1, e2) -> {
			if (e1.flow == null || e2.flow == null)
				return 0;
			return Strings.compare(e1.flow.name, e2.flow.name);
		});
		return exchanges;
	}
}
