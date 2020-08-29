package org.openlca.io.xls.process.output;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Unit;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;
import org.openlca.util.Strings;

class IOSheet {

	private final Config config;
	private final Sheet sheet;
	private final boolean forInputs;
	private int row = 0;

	private IOSheet(Config config, boolean forInputs) {
		this.config = config;
		this.forInputs = forInputs;
		String sheetName = forInputs ? "Inputs" : "Outputs";
		sheet = config.workbook.createSheet(sheetName);
	}

	public static void writeInputs(Config config) {
		new IOSheet(config, true).write();
	}

	public static void writeOutputs(Config config) {
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
		config.header(sheet, row, 0, "Flow");
		config.header(sheet, row, 1, "Category");
		config.header(sheet, row, 2, "Flow property");
		config.header(sheet, row, 3, "Unit");
		config.header(sheet, row, 4, "Amount");
		config.header(sheet, row, 5, "Formula");
		config.header(sheet, row, 6, "Description");
		config.header(sheet, row, 7, "Uncertainty");
		config.header(sheet, row, 8, "(g)mean | mode");
		config.header(sheet, row, 9, "SD | GSD");
		config.header(sheet, row, 10, "Minimum");
		config.header(sheet, row, 11, "Maximum");
		if (!forInputs)
			config.header(sheet, row, 12, "Is avoided product?");
	}

	private void write(Exchange exchange) {
		Flow flow = exchange.flow;
		Excel.cell(sheet, row, 0, flow.name);
		Excel.cell(sheet, row, 1, CategoryPath.getFull(flow.category));
		Excel.cell(sheet, row, 2, getFlowProperty(exchange));
		Excel.cell(sheet, row, 3, getUnit(exchange));
		Excel.cell(sheet, row, 4, exchange.amount);
		Excel.cell(sheet, row, 5, exchange.formula);
		Excel.cell(sheet, row, 6, exchange.description);
		config.uncertainty(sheet, row, 7, exchange.uncertainty);
		if (!forInputs)
			Excel.cell(sheet, row, 12, exchange.isAvoided ? "Yes": "");
	}

	private String getFlowProperty(Exchange exchange) {
		FlowPropertyFactor factor = exchange.flowPropertyFactor;
		if (factor == null)
			return null;
		FlowProperty prop = factor.flowProperty;
		return prop == null ? null : prop.name;
	}

	private String getUnit(Exchange exchange) {
		Unit unit = exchange.unit;
		return unit == null ? null : unit.name;
	}

	private List<Exchange> getExchanges() {
		var exchanges = new ArrayList<Exchange>();
		for (Exchange exchange : config.process.exchanges) {
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
