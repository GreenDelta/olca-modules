package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Unit;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class IOSheet {

	public static void readInputs(final Config config) {
		new IOSheet(config, true).read();
	}

	public static void readOutputs(final Config config) {
		new IOSheet(config, false).read();
	}

	private final Config config;
	private final boolean forInputs;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Sheet sheet;

	private IOSheet(final Config config, final boolean forInputs) {
		this.config = config;
		this.forInputs = forInputs;
		final String sheetName = forInputs ? "Inputs" : "Outputs";
		sheet = config.workbook.getSheet(sheetName);
	}

	private void read() {
		if (sheet == null) {
			return;
		}
		try {
			log.trace("read exchanges; inputs={}", forInputs);
			int row = 1;
			while (true) {
				final Exchange exchange = readExchange(row);
				if (exchange == null) {
					break;
				}
				config.process.getExchanges().add(exchange);
				row++;
			}
		} catch (final Exception e) {
			log.error("failed to read exchanges", e);
		}
	}

	private Exchange readExchange(final int row) {
		final RefData refData = config.refData;
		final Exchange exchange = new Exchange();
		exchange.setInput(forInputs);
		final String name = config.getString(sheet, row, 0);
		if (name == null) {
			return null;
		}
		final String category = config.getString(sheet, row, 1);
		final Flow flow = refData.getFlow(name, category);
		if (flow == null) {
			return refDataError(row, "flow: " + name + "/" + category);
		}
		exchange.setFlow(flow);
		final String propName = config.getString(sheet, row, 2);
		final FlowProperty property = refData.getFlowProperty(propName);
		if (property == null) {
			return refDataError(row, "flow property: " + propName);
		}
		final FlowPropertyFactor factor = flow.getFactor(property);
		if (factor == null) {
			return refDataError(row, "flow property factor: " + propName);
		}
		exchange.setFlowPropertyFactor(factor);
		final String unitName = config.getString(sheet, row, 3);
		final Unit unit = refData.getUnit(unitName);
		if (unit == null) {
			return refDataError(row, "unit: " + unitName);
		}
		exchange.setUnit(unit);
		exchange.setAmountValue(config.getDouble(sheet, row, 4));
		final String formula = config.getString(sheet, row, 5);
		if (!Strings.nullOrEmpty(formula)) {
			exchange.setAmountFormula(formula);
		}
		exchange.setUncertainty(config.getUncertainty(sheet, row, 6));
		return exchange;
	}

	private Exchange refDataError(final int row, final String message) {
		log.error("could not create an exchange because of missing reference "
				+ "datum: {}; forInputs={} row={}", message, forInputs, row);
		return null;
	}

}
