package org.openlca.io.ilcd.input;

import org.openlca.core.model.Exchange;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.util.ExchangeExtension;

/**
 * A helper class for the conversion of ILCD exchanges to openLCA exchanges. The
 * map-function of this class creates an openLCA exchange and maps the
 * non-reference values to this exchange. Non-reference values means simple
 * attributes (numbers, booleans etc.) in contrast to units, flows etc. which
 * are not mapped in this class.
 */
class ExchangeConversion {

	private ImportConfig config;
	private org.openlca.ilcd.processes.Exchange ilcdExchange;
	private ExchangeExtension extension;
	private Exchange olcaExchange;

	public ExchangeConversion(org.openlca.ilcd.processes.Exchange ilcdExchange, ImportConfig config) {
		this.ilcdExchange = ilcdExchange;
		this.config = config;
		ExchangeExtension ext = new ExchangeExtension(ilcdExchange);
		if (ext.isValid())
			extension = ext;
	}

	public Exchange map(ExchangeFlow exchangeFlow) {
		olcaExchange = initExchange(exchangeFlow);
		new UncertaintyConverter().map(ilcdExchange, olcaExchange);
		if (isParameterized())
			mapFormula();
		return olcaExchange;
	}

	private Exchange initExchange(ExchangeFlow iEx) {
		Exchange e = null;
		if (iEx.flowProperty != null && iEx.unit != null) {
			e = iEx.process.exchange(iEx.flow, iEx.flowProperty, iEx.unit);
		} else {
			e = iEx.process.exchange(iEx.flow);
		}
		boolean input = ilcdExchange.direction == ExchangeDirection.INPUT;
		e.isInput = input;
		e.description = LangString.getFirst(ilcdExchange.comment,
				config.langs);
		if (extension != null) {
			e.dqEntry = extension.getPedigreeUncertainty();
			e.baseUncertainty = extension.getBaseUncertainty();
			e.amount = extension.getAmount();
		} else {
			Double amount = ilcdExchange.resultingAmount;
			if (amount != null)
				e.amount = amount;
		}
		return e;
	}

	private boolean isParameterized() {
		return ilcdExchange.variable != null
				|| (extension != null && extension.getFormula() != null);
	}

	private void mapFormula() {
		String formula = extension != null ? extension.getFormula() : null;
		if (formula != null)
			olcaExchange.amountFormula = formula;
		else {
			double meanAmount = ilcdExchange.meanAmount;
			String meanAmountStr = Double.toString(meanAmount);
			String parameter = ilcdExchange.variable;
			formula = meanAmount == 1.0 ? parameter : meanAmountStr + " * "
					+ parameter + "";
			olcaExchange.amountFormula = formula;
		}
	}

}
