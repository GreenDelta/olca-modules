package org.openlca.io.ilcd.output;

import java.math.BigDecimal;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.ilcd.commons.UncertaintyDistribution;
import org.openlca.ilcd.util.ExchangeExtension;

/**
 * Maps the uncertainty information from an openLCA exchange to an ILCD
 * exchange.
 */
class UncertaintyConverter {

	public void map(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		Uncertainty uncertainty = oExchange.getUncertainty();
		if (uncertainty == null
				|| uncertainty.getDistributionType() == UncertaintyType.NONE)
			return;
		switch (uncertainty.getDistributionType()) {
		case LOG_NORMAL:
			mapLogNormal(oExchange, iExchange);
			break;
		case NORMAL:
			mapNormal(oExchange, iExchange);
			break;
		case TRIANGLE:
			mapTriangular(oExchange, iExchange);
			break;
		case UNIFORM:
			mapUniform(oExchange, iExchange);
			break;
		default:
			break;
		}
	}

	private void mapLogNormal(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		Double param = getUncertaintyParam(2, oExchange);
		if (param == null)
			return;
		BigDecimal decimal = new BigDecimal(param);
		iExchange.setRelativeStandardDeviation95In(decimal);
		iExchange
				.setUncertaintyDistribution(UncertaintyDistribution.LOG_NORMAL);
	}

	private void mapNormal(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		Double param = getUncertaintyParam(2, oExchange);
		if (param == null)
			return;
		BigDecimal decimal = new BigDecimal(param);
		iExchange.setRelativeStandardDeviation95In(decimal);
		iExchange.setUncertaintyDistribution(UncertaintyDistribution.NORMAL);
	}

	private void mapTriangular(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		Double param1 = getUncertaintyParam(1, oExchange);
		Double param2 = getUncertaintyParam(2, oExchange);
		Double param3 = getUncertaintyParam(3, oExchange);
		if (param1 == null || param2 == null || param3 == null)
			return;
		iExchange.setMinimumAmount(param1);
		new ExchangeExtension(iExchange).setMostLikelyValue(param2);
		iExchange.setMaximumAmount(param3);
		iExchange
				.setUncertaintyDistribution(UncertaintyDistribution.TRIANGULAR);
	}

	private void mapUniform(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		Double param1 = getUncertaintyParam(1, oExchange);
		Double param2 = getUncertaintyParam(2, oExchange);
		if (param1 == null || param2 == null)
			return;
		iExchange.setMinimumAmount(param1);
		iExchange.setMaximumAmount(param2);
		iExchange.setUncertaintyDistribution(UncertaintyDistribution.UNIFORM);
	}

	private Double getUncertaintyParam(int param, Exchange oExchange) {
		Uncertainty uncertainty = oExchange.getUncertainty();
		switch (param) {
		case 1:
			return uncertainty.getParameter1Value();
		case 2:
			return uncertainty.getParameter2Value();
		case 3:
			return uncertainty.getParameter3Value();
		default:
			return null;
		}
	}
}
