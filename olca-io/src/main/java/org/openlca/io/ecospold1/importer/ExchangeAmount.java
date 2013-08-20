package org.openlca.io.ecospold1.importer;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.UncertaintyDistributionType;
import org.openlca.ecospold.IExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps the amount and uncertainty distribution parameters of an EcoSpold 01
 * exchange to an openLCA exchange.
 */
class ExchangeAmount {

	private Exchange olcaExchange;
	private IExchange esExchange;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public ExchangeAmount(Exchange olcaExchange, IExchange esExchange) {
		this.olcaExchange = olcaExchange;
		this.esExchange = esExchange;
	}

	public void map(double factor) {
		try {
			double mean = esExchange.getMeanValue() * factor;
			olcaExchange.setAmountValue(mean);
			if (esExchange.getUncertaintyType() == null) {
				olcaExchange
						.setDistributionType(UncertaintyDistributionType.NONE);
			} else {
				setUncertaintyValues(mean);
			}
		} catch (Exception e) {
			log.error("Mapping uncertainty distribution failed", e);
		}
	}

	private void setUncertaintyValues(double mean) {
		switch (esExchange.getUncertaintyType()) {
		case 1:
			mapLogNormal(mean, esExchange.getStandardDeviation95());
			break;
		case 2:
			mapNormal(mean, esExchange.getStandardDeviation95());
			break;
		case 3:
			mapTriangle(mean, esExchange.getMinValue(),
					esExchange.getMaxValue());
			break;
		case 4:
			mapUniform(esExchange.getMinValue(), esExchange.getMaxValue());
			break;
		}
	}

	private void mapUniform(Double min, Double max) {
		if (min == null || max == null)
			return;
		olcaExchange.setDistributionType(UncertaintyDistributionType.UNIFORM);
		olcaExchange.setParameter1Value(min);
		olcaExchange.setParameter2Value(max);
	}

	private void mapTriangle(double mean, Double min, Double max) {
		if (min == null || max == null)
			return;
		olcaExchange.setDistributionType(UncertaintyDistributionType.TRIANGLE);
		olcaExchange.setParameter1Value(min);
		Double mostLikely = esExchange.getMostLikelyValue();
		if (mostLikely == null) {
			mostLikely = 3 * mean - min - max;
		}
		olcaExchange.setParameter2Value(mostLikely);
		olcaExchange.setParameter3Value(max);
	}

	private void mapNormal(double mean, Double sd) {
		if (sd == null)
			return;
		olcaExchange.setDistributionType(UncertaintyDistributionType.NORMAL);
		olcaExchange.setParameter1Value(mean);
		olcaExchange.setParameter2Value(sd / 2);
	}

	private void mapLogNormal(double mean, Double sd) {
		if (sd == null)
			return;
		olcaExchange
				.setDistributionType(UncertaintyDistributionType.LOG_NORMAL);
		olcaExchange.setParameter1Value(mean);
		olcaExchange.setParameter2Value(Math.sqrt(sd));
	}

}
