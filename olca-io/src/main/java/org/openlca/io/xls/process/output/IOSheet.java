package org.openlca.io.xls.process.output;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.poi.ss.usermodel.Row;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;
import org.openlca.util.Exchanges;
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
		for (var e : getExchanges()) {
			if (e.flow == null)
				continue;
			// visit dependencies
			wb.visit(e.flow);
			wb.visit(e.currency);
			wb.visit(e.location);
			write(e);
		}
	}

	private void writeHeader() {
		cursor.header(
				"Is reference?", // 0
				"Flow", // 1
				"Category", // 2
				"Amount", // 3
				"Unit", // 4
				"Costs/Revenues", // 5
				"Currency", // 6
				"Uncertainty", // 7
				"(G)Mean | Mode", // 8
				"SD | GSD", // 9
				"Minimum", // 10
				"Maximum", // 11
				"Is avoided?", // 12
				"Provider", // 13
				"Data quality entry", // 14
				"Location", // 15
				"Description"); // 16
	}

	private void write(Exchange e) {
		boolean isRef = Objects.equals(e, wb.process.quantitativeReference);
		var rowRef = new AtomicReference<Row>();
		cursor.next(row -> {
			rowRef.set(row);

			// flow, amount, unit
			Excel.cell(row, 0, isRef ? "x" : null);
			Excel.cell(row, 1, e.flow.name);
			Excel.cell(row, 2, CategoryPath.getFull(e.flow.category));
			if (Strings.notEmpty(e.formula)) {
				Excel.cell(row, 3, e.formula);
			} else {
				Excel.cell(row, 3, e.amount);
			}
			Excel.cell(row, 4, e.unit != null ? e.unit.name : null);

			// costs
			if (e.currency != null) {
				boolean hasCosts = false;
				if (Strings.notEmpty(e.costFormula)) {
					Excel.cell(row, 5, e.costFormula);
					hasCosts = true;
				} else if (e.costs != null) {
					Excel.cell(row, 5, e.costs);
					hasCosts = true;
				}
				if (hasCosts) {
					Excel.cell(row, 6, e.currency.name);
				}
			}

			// provider
			if (e.defaultProviderId > 0 && Exchanges.isProviderFlow(e)) {
				var provider = wb.db.get(Process.class, e.defaultProviderId);
				if (provider != null) {
					wb.visit(provider);
					Excel.cell(row, 13, provider.name);
				}
			}

			// other fields
			Util.write(row, 7, e.uncertainty);
			Excel.cell(row, 12, e.isAvoided ? "x" : null);
			Excel.cell(row, 14, e.dqEntry);
			if (e.location != null) {
				Excel.cell(row, 15, e.location.name);
			}
			Excel.cell(row, 16, e.description);
		});

		if (isRef && rowRef.get() != null) {
			var row = rowRef.get();
			row.cellIterator()
					.forEachRemaining(cell -> cell.setCellStyle(wb.boldFont));
		}
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
