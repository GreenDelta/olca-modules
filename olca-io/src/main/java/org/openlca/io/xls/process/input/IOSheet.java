package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Unit;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class IOSheet {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final Config config;
	private final boolean forInputs;
	private final Sheet sheet;

	private IOSheet(Config config, boolean forInputs) {
		this.config = config;
		this.forInputs = forInputs;
		String sheetName = forInputs ? "Inputs" : "Outputs";
		sheet = config.workbook.getSheet(sheetName);
	}

	public static void readInputs(Config config) {
		new IOSheet(config, true).read();
	}

	public static void readOutputs(Config config) {
		new IOSheet(config, false).read();
	}

	private void read() {
		if (sheet == null) {
			return;
		}
		try {
			log.trace("read exchanges; inputs={}", forInputs);
			int row = 1;
			while (true) {
				Exchange exchange = readExchange(row);
				if (exchange == null) {
					break;
				}
				row++;
			}
		} catch (Exception e) {
			log.error("failed to read exchanges", e);
		}
	}

	private Exchange readExchange(int row) {
		RefData refData = config.refData;
		String name = config.getString(sheet, row, 0);
		if (name == null) {
			return null;
		}
		String category = config.getString(sheet, row, 1);
		Flow flow = refData.getFlow(name, category);
		if (flow == null) {
			return refDataError(row, "flow: " + name + "/" + category);
		}
		String propName = config.getString(sheet, row, 2);
		FlowProperty property = refData.getFlowProperty(propName);
		if (property == null) {
			return refDataError(row, "flow property: " + propName);
		}
		if (flow.getFactor(property) == null) {
			return refDataError(row, "flow property factor: " + propName);
		}
		String unitName = config.getString(sheet, row, 3);
		Unit unit = refData.getUnit(unitName);
		if (unit == null) {
			return refDataError(row, "unit: " + unitName);
		}
		Exchange exchange = config.process.exchange(flow, property, unit);
		exchange.isInput = forInputs;
		exchange.amount = config.getDouble(sheet, row, 4);
		String formula = config.getString(sheet, row, 5);
		if (!Strings.nullOrEmpty(formula)) {
			exchange.amountFormula = formula;
		}
		String description = config.getString(sheet, row, 6);
		if (!Strings.nullOrEmpty(description)) {
			exchange.description = description;
		}
		exchange.uncertainty = config.getUncertainty(sheet, row, 7);
		if ("Yes".equals(config.getString(sheet, row, 12)))
			exchange.isAvoided = true;
		return exchange;
	}

	private Exchange refDataError(int row, String message) {
		log.error("could not create an exchange because of missing reference "
				+ "datum: {}; forInputs={} row={}", message, forInputs, row);
		return null;
	}

}
