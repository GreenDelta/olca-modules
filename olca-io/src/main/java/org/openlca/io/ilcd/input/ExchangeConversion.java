package org.openlca.io.ilcd.input;

import org.openlca.core.model.Exchange;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.util.ExchangeExtension;

/**
 * A helper class for the conversion of ILCD exchanges to openLCA exchanges. The
 * map-function of this class creates an openLCA exchange and maps the
 * non-reference values to this exchange. Non-reference values means simple
 * attributes (numbers, booleans etc.) in contrast to units, flows etc. which
 * are not mapped in this class.
 */
class ExchangeConversion {

	private org.openlca.ilcd.processes.Exchange ilcdExchange;
	private ExchangeExtension extension;
	private Exchange olcaExchange;

	public ExchangeConversion(org.openlca.ilcd.processes.Exchange ilcdExchange) {
		this.ilcdExchange = ilcdExchange;
		ExchangeExtension ext = new ExchangeExtension(ilcdExchange);
		if (ext.isValid())
			extension = ext;
	}

	public Exchange map() {
		olcaExchange = initExchange();
		new UncertaintyConverter().map(ilcdExchange, olcaExchange);
		if (isParameterized())
			mapFormula();
		return olcaExchange;
	}

	private Exchange initExchange() {
		Exchange e = new Exchange();
		boolean input = ilcdExchange.getExchangeDirection() == ExchangeDirection.INPUT;
		e.setInput(input);
		if (extension != null) {
			e.setPedigreeUncertainty(extension.getPedigreeUncertainty());
			e.setBaseUncertainty(extension.getBaseUncertainty());
			olcaExchange.setAmountValue(extension.getAmount());
		} else {
			Double amount = ilcdExchange.getResultingAmount();
			if (amount != null)
				olcaExchange.setAmountValue(amount);
		}
		return e;
	}

	private boolean isParameterized() {
		return ilcdExchange.getParameterName() != null
				|| (extension != null && extension.getFormula() != null);
	}

	private void mapFormula() {
		String formula = extension != null ? extension.getFormula() : null;
		if (formula != null)
			olcaExchange.setAmountFormula(formula);
		else {
			double meanAmount = ilcdExchange.getMeanAmount();
			String meanAmountStr = Double.toString(meanAmount);
			String parameter = ilcdExchange.getParameterName();
			formula = meanAmount == 1.0 ? parameter : meanAmountStr + " * "
					+ parameter + "";
			olcaExchange.setAmountFormula(formula);
		}
	}

}
