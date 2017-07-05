package org.openlca.io.ilcd.output;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.ilcd.commons.UncertaintyDistribution;
import org.openlca.ilcd.util.ExchangeExtension;

/**
 * Maps the uncertainty information from exchanges and parameters.
 */
class UncertaintyConverter {

	public void map(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		Uncertainty uncertainty = oExchange.uncertainty;
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

	public void map(Parameter oParameter,
			org.openlca.ilcd.processes.Parameter iParameter) {
		Uncertainty uncertainty = oParameter.getUncertainty();
		if (uncertainty == null
				|| uncertainty.getDistributionType() == UncertaintyType.NONE)
			return;
		switch (uncertainty.getDistributionType()) {
		case LOG_NORMAL:
			mapLogNormal(oParameter, iParameter);
			break;
		case NORMAL:
			mapNormal(oParameter, iParameter);
			break;
		case TRIANGLE:
			mapTriangle(oParameter, iParameter);
			break;
		case UNIFORM:
			mapUniform(oParameter, iParameter);
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
		iExchange.relativeStandardDeviation95In = param;
		iExchange.uncertaintyDistribution = UncertaintyDistribution.LOG_NORMAL;
	}

	private void mapLogNormal(Parameter oParameter,
			org.openlca.ilcd.processes.Parameter iParameter) {
		Double std = oParameter.getUncertainty().getParameter2Value();
		if (std == null)
			return;
		iParameter.dispersion = std;
		iParameter.distribution = UncertaintyDistribution.LOG_NORMAL;
	}

	private void mapNormal(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		Double param = getUncertaintyParam(2, oExchange);
		if (param == null)
			return;
		iExchange.relativeStandardDeviation95In = param;
		iExchange.uncertaintyDistribution = UncertaintyDistribution.NORMAL;
	}

	private void mapNormal(Parameter oParameter,
			org.openlca.ilcd.processes.Parameter iParameter) {
		Double std = oParameter.getUncertainty().getParameter2Value();
		if (std == null)
			return;
		iParameter.dispersion = std;
		iParameter.distribution = UncertaintyDistribution.NORMAL;
	}

	private void mapTriangular(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		Double param1 = getUncertaintyParam(1, oExchange);
		Double param2 = getUncertaintyParam(2, oExchange);
		Double param3 = getUncertaintyParam(3, oExchange);
		if (param1 == null || param2 == null || param3 == null)
			return;
		iExchange.minimumAmount = param1;
		new ExchangeExtension(iExchange).setMostLikelyValue(param2);
		iExchange.maximumAmount = param3;
		iExchange.uncertaintyDistribution = UncertaintyDistribution.TRIANGULAR;
	}

	private void mapTriangle(Parameter oParameter,
			org.openlca.ilcd.processes.Parameter iParameter) {
		Double min = oParameter.getUncertainty().getParameter1Value();
		// Double mode = oParameter.getUncertainty().getParameter2Value();
		// TODO: ILCD do not provide a field for the mode, we have to add
		// an extension to the format
		Double max = oParameter.getUncertainty().getParameter3Value();
		if (min == null || max == null)
			return;
		iParameter.min = min;
		iParameter.max = max;
		iParameter.distribution = UncertaintyDistribution.TRIANGULAR;
	}

	private void mapUniform(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		Double param1 = getUncertaintyParam(1, oExchange);
		Double param2 = getUncertaintyParam(2, oExchange);
		if (param1 == null || param2 == null)
			return;
		iExchange.minimumAmount = param1;
		iExchange.maximumAmount = param2;
		iExchange.uncertaintyDistribution = UncertaintyDistribution.UNIFORM;
	}

	private void mapUniform(Parameter oParameter,
			org.openlca.ilcd.processes.Parameter iParameter) {
		Double min = oParameter.getUncertainty().getParameter1Value();
		Double max = oParameter.getUncertainty().getParameter2Value();
		if (min == null || max == null)
			return;
		iParameter.min = min;
		iParameter.max = max;
		iParameter.distribution = UncertaintyDistribution.UNIFORM;
	}

	private Double getUncertaintyParam(int param, Exchange oExchange) {
		Uncertainty uncertainty = oExchange.uncertainty;
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
