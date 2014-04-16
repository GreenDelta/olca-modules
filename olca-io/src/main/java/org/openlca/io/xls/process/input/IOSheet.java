package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class IOSheet {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final Config config;
	private final Sheet sheet;
	private final boolean forInputs;

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
		try {
			log.trace("read exchanges; inputs={}", forInputs);
			int row = 1;
			while(true) {
				Exchange exchange = readExchange(row);
				if(exchange == null)
					break;
				config.process.getExchanges().add(exchange);
				row++;
			}
		} catch (Exception e) {
			log.error("failed to read exchanges", e);
		}
	}

	private Exchange readExchange(int row) {
		RefData refData = config.refData;
		Exchange exchange = new Exchange();
		exchange.setInput(forInputs);
		String name = config.getString(sheet, row, 0);
		if(name == null)
			return null;
		String category = config.getString(sheet, row, 1);
		Flow flow = refData.getFlow(name, category);
		if(flow == null)
			return refDataError(row, "flow: " + name + "/" + category);
		exchange.setFlow(flow);
		String propName = config.getString(sheet, row, 2);
		FlowProperty property = refData.getFlowProperty(propName);
		if(property == null)
			return  refDataError(row, "flow property: " + propName);
		FlowPropertyFactor factor = flow.getFactor(property);
		if(factor == null)
			return refDataError(row, "flow property factor: " + propName);
		exchange.setFlowPropertyFactor(factor);
		String unitName = config.getString(sheet, row, 3);
		Unit unit = refData.getUnit(unitName);
		if(unit == null)
			return refDataError(row, "unit: " + unitName);
		exchange.setUnit(unit);
		exchange.setAmountValue(config.getDouble(sheet, row, 4));
		// TODO: read uncertainty
		return exchange;
	}

	private Exchange refDataError(int row, String message) {
		log.error("could not create an exchange because of missing reference " +
				"datum: {}; forInputs={} row={}", message, forInputs, row);
		return null;
	}

}
