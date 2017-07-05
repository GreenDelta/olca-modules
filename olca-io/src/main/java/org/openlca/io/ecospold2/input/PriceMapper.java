package org.openlca.io.ecospold2.input;

import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spold2.IntermediateExchange;
import spold2.Property;

/**
 * Maps prices of ecoinvent intermediate exchanges to cost information of the
 * respective openLCA inputs and outputs.
 */
class PriceMapper {

	private Currency currency;

	PriceMapper(IDatabase db) {
		Logger log = LoggerFactory.getLogger(getClass());
		try {
			CurrencyDao dao = new CurrencyDao(db);
			for (Currency c : dao.getAll()) {
				if ("EUR".equalsIgnoreCase(c.code)) {
					currency = c;
					break;
				}
			}
			if (currency == null) {
				log.info("no currency EUR found, will not map prices in import");
			}
		} catch (Exception e) {
			log.error("failed to get currency");
		}
	}

	void map(IntermediateExchange ie, Exchange e) {
		if (currency == null || ie == null || ie.amount == null || e == null)
			return;
		Property price = findPrice(ie);
		if (price == null)
			return;
		double val = ie.amount * price.amount;
		if (val == 0)
			return;
		e.costs = val;
		e.currency = currency;
	}

	private Property findPrice(IntermediateExchange ie) {
		for (Property p : ie.properties) {
			String name = p.name;
			String unit = p.unit;
			if (name == null || unit == null)
				continue;
			if ("price".equalsIgnoreCase(name) && unit.startsWith("EUR"))
				return p;
		}
		return null;
	}
}
