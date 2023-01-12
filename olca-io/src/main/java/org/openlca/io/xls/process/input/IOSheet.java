package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Row;
import org.openlca.core.model.Exchange;
import org.openlca.io.xls.process.Field;
import org.openlca.io.xls.process.Tab;
import org.openlca.util.Strings;

class IOSheet {

	private final ProcessWorkbook wb;
	private final boolean forInputs;
	private final SheetReader sheet;

	private IOSheet(ProcessWorkbook wb, boolean forInputs) {
		this.wb = wb;
		this.forInputs = forInputs;
		sheet = wb.reader()
				.getSheet(forInputs ? Tab.INPUTS : Tab.OUTPUTS )
				.orElse(null);
	}

	public static void readInputs(ProcessWorkbook wb) {
		new IOSheet(wb, true).read();
	}

	public static void readOutputs(ProcessWorkbook wb) {
		new IOSheet(wb, false).read();
	}

	private void read() {
		if (sheet == null)
			return;
		sheet.eachRow(this::nextExchange);
	}

	private void nextExchange(RowReader row) {
		var name = row.str(Field.NAME);
		if (name == null)
			return;

		var category = row.str(Field.CATEGORY);
		var flow = wb.index.getFlow(name, category);
		if (flow == null) {
			logErr(row, "flow: " + name + "/" + category);
			return;
		}

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



}
