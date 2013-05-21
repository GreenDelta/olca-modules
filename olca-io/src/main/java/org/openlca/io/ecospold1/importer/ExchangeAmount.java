package org.openlca.io.ecospold1.importer;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Expression;
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
			olcaExchange.getResultingAmount().setFormula(Double.toString(mean));
			olcaExchange.getResultingAmount().setValue(mean);
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
		olcaExchange.setDistributionType(UncertaintyDistributionType.UNIFORM);
		Expression uMin = olcaExchange.getUncertaintyParameter1();
		Expression uMax = olcaExchange.getUncertaintyParameter2();
		uMin.setFormula(Double.toString(min));
		uMin.setValue(min);
		uMax.setFormula(Double.toString(max));
		uMax.setValue(max);
	}

	private void mapTriangle(double mean, Double min, Double max) {
		olcaExchange.setDistributionType(UncertaintyDistributionType.TRIANGLE);
		Expression tMin = olcaExchange.getUncertaintyParameter1();
		Expression tMax = olcaExchange.getUncertaintyParameter2();
		Expression tMost = olcaExchange.getUncertaintyParameter3();
		tMin.setFormula(Double.toString(min));
		tMin.setValue(min);
		tMax.setFormula(Double.toString(max));
		tMax.setValue(max);
		Double mostLikely = esExchange.getMostLikelyValue();
		if (mostLikely == null) {
			mostLikely = 3 * mean - min - max;
		}
		tMost.setFormula(mostLikely.toString());
		tMost.setValue(mostLikely);
	}

	private void mapNormal(double mean, Double sd) {
		olcaExchange.setDistributionType(UncertaintyDistributionType.NORMAL);
		Expression aMean = olcaExchange.getUncertaintyParameter1();
		Expression asd = olcaExchange.getUncertaintyParameter2();
		aMean.setFormula(Double.toString(mean));
		aMean.setValue(mean);
		asd.setFormula(Double.toString(sd / 2));
		asd.setValue(sd / 2);
	}

	private void mapLogNormal(double mean, Double sd) {
		olcaExchange
				.setDistributionType(UncertaintyDistributionType.LOG_NORMAL);
		Expression geoMean = olcaExchange.getUncertaintyParameter1();
		Expression geoSD = olcaExchange.getUncertaintyParameter2();
		geoMean.setFormula(Double.toString(mean));
		geoMean.setValue(mean);
		geoSD.setFormula(Double.toString(Math.sqrt(sd)));
		geoSD.setValue(Math.sqrt(sd));
	}

}
