package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Unit;
import org.openlca.util.Strings;

class IOSheet {

	private final ProcessWorkbook wb;
	private final boolean forInputs;
	private final Sheet sheet;

	private IOSheet(ProcessWorkbook wb, boolean forInputs) {
		this.wb = wb;
		this.forInputs = forInputs;
		String sheetName = forInputs ? "Inputs" : "Outputs";
		sheet = wb.getSheet(sheetName);
	}

	public static void readInputs(ProcessWorkbook wb) {
		new IOSheet(wb, true).read();
	}

	public static void readOutputs(ProcessWorkbook wb) {
		new IOSheet(wb, false).read();
	}

	private void read() {
		if (sheet == null) {
			return;
		}
		try {
			int row = 1;
			while (true) {
				Exchange exchange = readExchange(row);
				if (exchange == null) {
					break;
				}
				row++;
			}
		} catch (Exception e) {
			wb.log.error("failed to read exchanges", e);
		}
	}

	private Exchange readExchange(int row) {
		String name = wb.getString(sheet, row, 0);
		if (name == null) {
			return null;
		}

		String category = wb.getString(sheet, row, 1);
		Flow flow = wb.index.getFlow(name, category);
		if (flow == null) {
			return refDataError(row, "flow: " + name + "/" + category);
		}

		String propName = wb.getString(sheet, row, 2);
		FlowProperty property = wb.index.get(FlowProperty.class, propName);
		if (property == null) {
			return refDataError(row, "flow property: " + propName);
		}

		var factor = flow.getFactor(property);
		if (factor == null) {
			return refDataError(row, "flow property factor: " + propName);
		}
		String unitName = wb.getString(sheet, row, 3);
		Unit unit = wb.index.unitOf(factor, unitName);
		if (unit == null) {
			return refDataError(row, "unit: " + unitName);
		}

		var exchange = wb.process.add(Exchange.of(flow, property, unit));
		exchange.isInput = forInputs;
		exchange.amount = wb.getDouble(sheet, row, 4);
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
		return exchange;
	}

	private Exchange refDataError(int row, String m) {
		wb.log.error("could not create an exchange because of missing reference "
				+ "datum: " + m + "; forInputs=" + forInputs + " row=" + row);
		return null;
	}

}
