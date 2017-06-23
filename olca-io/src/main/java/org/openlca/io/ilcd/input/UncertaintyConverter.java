package org.openlca.io.ilcd.input;

import org.openlca.core.model.Uncertainty;
import org.openlca.ilcd.commons.UncertaintyDistribution;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Parameter;
import org.openlca.ilcd.util.ExchangeExtension;

/**
 * Maps uncertainty information of exchanges and parameters.
 */
class UncertaintyConverter {

	public void map(Exchange iExchange,
			org.openlca.core.model.Exchange oExchange) {
		if (iExchange.uncertaintyDistribution == null
				|| iExchange.uncertaintyDistribution == UncertaintyDistribution.UNDEFINED)
			return;
		switch (iExchange.uncertaintyDistribution) {
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

	public void map(Parameter iParameter,
			org.openlca.core.model.Parameter oParameter) {
		if (iParameter.distribution == null
				|| iParameter.distribution == UncertaintyDistribution.UNDEFINED)
			return;
		switch (iParameter.distribution) {
		case LOG_NORMAL:
			mapLogNormal(iParameter, oParameter);
			break;
		case NORMAL:
			mapNormal(iParameter, oParameter);
			break;
		case TRIANGULAR:
			mapTriangular(iParameter, oParameter);
			break;
		case UNIFORM:
			mapUniform(iParameter, oParameter);
			break;
		default:
			break;
		}
	}

	private void mapLogNormal(Exchange iExchange,
			org.openlca.core.model.Exchange oExchange) {
		double mean = getAmount(iExchange);
		Double sd = iExchange.relativeStandardDeviation95In;
		if (sd == null)
			return;
		oExchange.uncertainty = Uncertainty.logNormal(mean, sd);
	}

	private void mapLogNormal(Parameter iParameter,
			org.openlca.core.model.Parameter oParameter) {
		Double mean = iParameter.mean;
		Double std = iParameter.dispersion;
		if (mean == null || std == null)
			return;
		oParameter.setUncertainty(Uncertainty.logNormal(mean, std));
	}

	private void mapNormal(Exchange iExchange,
			org.openlca.core.model.Exchange oExchange) {
		double mean = getAmount(iExchange);
		Double sd = iExchange.relativeStandardDeviation95In;
		if (sd == null)
			return;
		oExchange.uncertainty = Uncertainty.normal(mean, sd);
	}

	private void mapNormal(Parameter iParameter,
			org.openlca.core.model.Parameter oParameter) {
		Double mean = iParameter.mean;
		Double std = iParameter.dispersion;
		if (mean == null || std == null)
			return;
		oParameter.setUncertainty(Uncertainty.normal(mean, std));
	}

	private void mapTriangular(Exchange iExchange,
			org.openlca.core.model.Exchange oExchange) {
		Double min = iExchange.minimumAmount;
		Double mode = new ExchangeExtension(iExchange).getMostLikelyValue();
		Double max = iExchange.maximumAmount;
		if (min == null || mode == null || max == null)
			return;
		oExchange.uncertainty = Uncertainty.triangle(min, mode, max);
	}

	private void mapTriangular(Parameter iParameter,
			org.openlca.core.model.Parameter oParameter) {
		Double min = iParameter.min;
		Double mean = iParameter.mean;
		Double max = iParameter.max;
		if (min == null || mean == null || max == null)
			return;
		oParameter.setUncertainty(Uncertainty.triangle(min, mean, max));
	}

	private void mapUniform(Exchange iExchange,
			org.openlca.core.model.Exchange oExchange) {
		Double min = iExchange.minimumAmount;
		Double max = iExchange.maximumAmount;
		if (min == null || max == null)
			return;
		oExchange.uncertainty = Uncertainty.uniform(min, max);
	}

	private void mapUniform(Parameter iParameter,
			org.openlca.core.model.Parameter oParameter) {
		Double min = iParameter.min;
		Double max = iParameter.max;
		if (min == null || max == null)
			return;
		oParameter.setUncertainty(Uncertainty.uniform(min, max));
	}

	private double getAmount(Exchange iExchange) {
		ExchangeExtension ext = new ExchangeExtension(iExchange);
		Double val = ext.getAmount();
		if (val != null)
			return val;
		val = iExchange.resultingAmount;
		if (val != null)
			return val;
		return iExchange.meanAmount;
	}

}
