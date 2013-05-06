package org.openlca.io.ilcd.output;

import java.math.BigDecimal;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Expression;
import org.openlca.core.model.UncertaintyDistributionType;
import org.openlca.ilcd.commons.UncertaintyDistribution;
import org.openlca.ilcd.util.ExchangeExtension;

/**
 * Maps the uncertainty information of an openLCA exchange to an ILCD exchange.
 */
class UncertaintyConverter {

	public void map(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		if (oExchange.getDistributionType() == null
				|| oExchange.getDistributionType() == UncertaintyDistributionType.NONE)
			return;
		switch (oExchange.getDistributionType()) {
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
		Expression exp = null;
		switch (param) {
		case 1:
			exp = oExchange.getUncertaintyParameter1();
			break;
		case 2:
			exp = oExchange.getUncertaintyParameter2();
			break;
		case 3:
			exp = oExchange.getUncertaintyParameter3();
			break;
		default:
			break;
		}
		if (exp == null)
			return null;
		return exp.getValue();
	}
}
