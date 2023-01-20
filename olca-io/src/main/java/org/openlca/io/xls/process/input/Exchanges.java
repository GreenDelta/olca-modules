package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Row;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Unit;
import org.openlca.io.xls.process.Field;
import org.openlca.io.xls.process.Tab;
import org.openlca.util.Strings;

import java.util.List;
import java.util.function.Consumer;

class Exchanges {

	private final ProcessWorkbook wb;

	private Exchanges(ProcessWorkbook wb) {
		this.wb = wb;
	}

	static void sync(ProcessWorkbook wb) {
		new Exchanges(wb).sync();
	}

	private void sync() {
		List.of(Tab.INPUTS, Tab.OUTPUTS).forEach(
				tab -> wb.reader().getSheet(tab).ifPresent(
						sheet -> sync(tab, sheet)));
		if (sheet == null)
			return;
		sheet.eachRow(this::nextExchange);
	}

	private void sync(Tab tab, SheetReader sheet) {

	}

	private void nextExchange(RowReader row) {
		var name = row.str(Field.NAME);
		if (name == null)
			return;

		var category = row.str(Field.CATEGORY);
		var flow = wb.index.getFlow(name, category);
		if (flow == null) {
			wb.log.error("unknown flow: " + category + "/" + name);
			return;
		}

		var exchange = new Exchange();
		exchange.flow = flow;

		var unitName = row.str(Field.UNIT);
		var prop = wb.index.flowPropertyOf(flow, unitName);
		var unit = wb.index.unitOf(factor, unitName);
		if (unit == null) {
			logErr(row, "unit: " + unitName);
			return;
		}

		var exchange = wb.process.add(Exchange.of(flow, prop, unit));
		exchange.isInput = forInputs;

		exchange.amount = row.num(Field.AMOUNT);

		String formula = wb.getString(sheet, row, 5);
		if (!Strings.nullOrEmpty(formula)) {
			exchange.formula = formula;
		}
		String description = wb.getString(sheet, row, 6);
		if (!Strings.nullOrEmpty(description)) {
			exchange.description = description;
		}
		exchange.uncertainty = wb.getUncertainty(sheet, row, 7);
		if ("Yes".equals(wb.getString(sheet, row, 12)))
			exchange.isAvoided = true;
	}


	private void logErr(Row row, String m) {
		wb.log.error("could not create an exchange because of missing reference "
				+ "datum: " + m + "; forInputs=" + forInputs + " row=" + row.getRowNum());
	}

	private void setUnit(Exchange e, RowReader row) {
		Consumer<String> err = message -> {
			wb.log.error(message + " in row " + row.getRowNum());
			e.unit = null;
			e.flowPropertyFactor = null;
		};
		if (e.flow == null) {
			err.accept("no flow -> no units");
			return;
		}
		var unitName = row.str(Field.UNIT);
		if (Strings.nullOrEmpty(unitName)) {
			err.accept("no unit defined");
			return;
		}

		Unit unit = null;
		FlowPropertyFactor factor = null;
		for (var f : e.flow.flowPropertyFactors) {
			if (f.flowProperty == null
					|| f.flowProperty.unitGroup == null)
				continue;
			var group = f.flowProperty.unitGroup;
			var u = group.getUnit(unitName);
			if (u != null) {
				unit = u;
				factor = f;
				break;
			}
		}

		if (unit == null) {
			err.accept("unknown unit " + unitName
					+ " for flow " + EntityIndex.flowKeyOf(e.flow));
			return;
		}
		e.unit = unit;
		e.flowPropertyFactor = factor;
	}

}
