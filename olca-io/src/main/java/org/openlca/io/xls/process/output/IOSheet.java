package org.openlca.io.xls.process.output;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;
import org.openlca.util.Strings;

class IOSheet {

	private final ProcessWorkbook wb;
	private final ProcessWorkbook.SheetCursor cursor;
	private final boolean forInputs;

	private IOSheet(ProcessWorkbook wb, boolean forInputs) {
		this.wb = wb;
		this.forInputs = forInputs;
		cursor = wb.createCursor(forInputs ? "Inputs" : "Outputs");
	}

	public static void writeInputs(ProcessWorkbook config) {
		new IOSheet(config, true).write();
	}

	public static void writeOutputs(ProcessWorkbook config) {
		new IOSheet(config, false).write();
	}

	private void write() {
		writeHeader();
		for (var exchange : getExchanges()) {
			if (exchange.flow == null)
				continue;
			write(exchange);
		}
	}

	private void writeHeader() {
		cursor.header(
				"Is reference?",
				"Flow",
				"Category",
				"Amount",
				"Unit",
				"Costs/Revenues",
				"Currency",
				"Uncertainty",
				"(G)Mean | Mode",
				"SD | GSD",
				"Minimum",
				"Maximum",
				"Is avoided?",
				"Provider",
				"Data quality entry",
				"Location",
				"Description");
	}

	private void write(Exchange e) {
		if (e.flow == null)
			return;

		// visit dependencies
		wb.visit(e.flow);
		wb.visit(e.currency);
		wb.visit(e.location);

		cursor.next(row -> {
			Excel.cell(row, 0, e.flow.name);
			Excel.cell(row, 1, CategoryPath.getFull(e.flow.category));
			Excel.cell(row, 2, getFlowProperty(e));
			Excel.cell(row, 3, e.unit != null ? e.unit.name : null);
			Excel.cell(row, 4, e.amount);
			Excel.cell(row, 5, e.formula);
			Excel.cell(row, 6, e.description);
			Util.write(row, 7, e.uncertainty);
			Excel.cell(row, 12, e.isAvoided ? "Yes" : "");
		});

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
