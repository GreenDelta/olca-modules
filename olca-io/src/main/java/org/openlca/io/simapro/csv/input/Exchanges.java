package org.openlca.io.simapro.csv.input;

import org.openlca.core.model.Exchange;
import org.openlca.simapro.csv.process.ExchangeRow;
import org.slf4j.LoggerFactory;

class Exchanges {

	static Exchange of(ProcessMapper p, SyncFlow f, ExchangeRow row) {
		if (f == null || f.flow() == null) {
			var log = LoggerFactory.getLogger(Exchanges.class);
			log.error(
				"could not create exchange as there was now flow found for {}", row);
			return null;
		}

		// init the exchange
		var e = new Exchange();
		p.process().lastInternalId++;
		e.internalId = p.process().lastInternalId;
		p.process().exchanges.add(e);
		e.description = row.comment();
		e.flow = f.flow();
		e.uncertainty = Uncertainties.of(row);

		// mapped flows
		if (f.isMapped()) {
			double factor = f.mapFactor();
			if (e.uncertainty != null) {
				e.uncertainty.scale(factor);
			}
			e.amount = factor * ProcessParameters.eval(p.formulaScope(), row.amount());
			if (row.amount().hasFormula()) {
				e.formula = factor + " * (" + row.amount().formula() + ")";
			}
			// TODO: SyncFlow should store the mapped unit and flow property
			e.flowPropertyFactor = f.flow().getReferenceFactor();
			e.unit = f.flow().getReferenceUnit();

		// unmapped flows
		} else {
			e.amount = ProcessParameters.eval(p.formulaScope(), row.amount());
			if (row.amount().hasFormula()) {
				e.formula = row.amount().formula();
			}
			var quantity = p.refData().quantityOf(row.unit());
			if (quantity != null) {
				e.unit = quantity.unit;
				e.flowPropertyFactor = f.flow().getFactor(quantity.flowProperty);
			}
		}
		return e;
	}
}
