package org.openlca.io.ecospold2.input;

import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Exchange;
import org.openlca.ecospold2.IntermediateExchange;
import org.openlca.ecospold2.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		if (currency == null || ie == null || ie.getAmount() == null || e == null)
			return;
		Property price = findPrice(ie);
		if (price == null)
			return;
		double val = ie.getAmount() * price.getAmount();
		if (val == 0)
			return;
		e.costValue = val;
		e.currency = currency;
	}

	private Property findPrice(IntermediateExchange ie) {
		for (Property p : ie.getProperties()) {
			String name = p.getName();
			String unit = p.getUnitName();
			if (name == null || unit == null)
				continue;
			if ("price".equalsIgnoreCase(name) && unit.startsWith("EUR"))
				return p;
		}
		return null;
	}
}
