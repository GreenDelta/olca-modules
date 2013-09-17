package org.openlca.io.ilcd.input;

import java.math.BigDecimal;

import org.openlca.core.model.Uncertainty;
import org.openlca.ilcd.commons.UncertaintyDistribution;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.util.ExchangeExtension;

/**
 * Maps uncertainty information of an ILCD exchange to an openLCA exchange.
 */
class UncertaintyConverter {

	public void map(Exchange iExchange,
			org.openlca.core.model.Exchange oExchange) {
		if (iExchange.getUncertaintyDistribution() == null
				|| iExchange.getUncertaintyDistribution() == UncertaintyDistribution.UNDEFINED)
			return;
		switch (iExchange.getUncertaintyDistribution()) {
		case LOG_NORMAL:
			mapLogNormal(iExchange, oExchange);
			break;
		case NORMAL:
			mapNormal(iExchange, oExchange);
			break;
		case TRIANGULAR:
			mapTriangular(iExchange, oExchange);
			break;
		case UNIFORM:
			mapUniform(iExchange, oExchange);
			break;
		default:
			break;
		}
	}

	private void mapLogNormal(Exchange iExchange,
			org.openlca.core.model.Exchange oExchange) {
		double mean = getAmount(iExchange);
		BigDecimal bigDec = iExchange.getRelativeStandardDeviation95In();
		if (bigDec == null)
			return;
		double s = bigDec.doubleValue();
		oExchange.setUncertainty(Uncertainty.logNormal(mean, s));
	}

	private void mapNormal(Exchange iExchange,
			org.openlca.core.model.Exchange oExchange) {
		double mean = getAmount(iExchange);
		BigDecimal bigDec = iExchange.getRelativeStandardDeviation95In();
		if (bigDec == null)
			return;
		double s = bigDec.doubleValue();
		oExchange.setUncertainty(Uncertainty.normal(mean, s));
	}

	private void mapTriangular(Exchange iExchange,
			org.openlca.core.model.Exchange oExchange) {
		Double min = iExchange.getMinimumAmount();
		Double mode = new ExchangeExtension(iExchange).getMostLikelyValue();
		Double max = iExchange.getMaximumAmount();
		if (min == null || mode == null || max == null)
			return;
		oExchange.setUncertainty(Uncertainty.triangle(min, mode, max));
	}

	private void mapUniform(Exchange iExchange,
			org.openlca.core.model.Exchange oExchange) {
		Double min = iExchange.getMinimumAmount();
		Double max = iExchange.getMaximumAmount();
		if (min == null || max == null)
			return;
		oExchange.setUncertainty(Uncertainty.uniform(min, max));
	}

	private double getAmount(Exchange iExchange) {
		ExchangeExtension ext = new ExchangeExtension(iExchange);
		Double val = ext.getAmount();
		if (val != null)
			return val;
		val = iExchange.getResultingAmount();
		if (val != null)
			return val;
		return iExchange.getMeanAmount();
	}

}
