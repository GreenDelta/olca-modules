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

	public void map(Exchange i, org.openlca.core.model.Exchange o) {
		if (i.getUncertaintyDistribution() == null
				|| i.getUncertaintyDistribution() == UncertaintyDistribution.UNDEFINED)
			return;
		switch (i.getUncertaintyDistribution()) {
			case LOG_NORMAL -> mapLogNormal(i, o);
			case NORMAL -> mapNormal(i, o);
			case TRIANGULAR -> mapTriangular(i, o);
			case UNIFORM -> mapUniform(i, o);
			default -> {
			}
		}
	}

	public void map(Parameter i, org.openlca.core.model.Parameter o) {
		if (i.getDistribution() == null
				|| i.getDistribution() == UncertaintyDistribution.UNDEFINED)
			return;
		switch (i.getDistribution()) {
			case LOG_NORMAL -> mapLogNormal(i, o);
			case NORMAL -> mapNormal(i, o);
			case TRIANGULAR -> mapTriangular(i, o);
			case UNIFORM -> mapUniform(i, o);
			default -> {
			}
		}
	}

	private void mapLogNormal(Exchange i, org.openlca.core.model.Exchange o) {
		double mean = getAmount(i);
		Double sd = i.getRelativeStandardDeviation95In();
		if (sd == null)
			return;
		o.uncertainty = Uncertainty.logNormal(mean, sd);
	}

	private void mapLogNormal(Parameter i, org.openlca.core.model.Parameter o) {
		Double mean = i.getMean();
		Double std = i.getDispersion();
		if (mean == null || std == null)
			return;
		o.uncertainty = Uncertainty.logNormal(mean, std);
	}

	private void mapNormal(Exchange i, org.openlca.core.model.Exchange o) {
		double mean = getAmount(i);
		Double sd = i.getRelativeStandardDeviation95In();
		if (sd == null)
			return;
		o.uncertainty = Uncertainty.normal(mean, sd);
	}

	private void mapNormal(Parameter i, org.openlca.core.model.Parameter o) {
		Double mean = i.getMean();
		Double std = i.getDispersion();
		if (mean == null || std == null)
			return;
		o.uncertainty = Uncertainty.normal(mean, std);
	}

	private void mapTriangular(Exchange i, org.openlca.core.model.Exchange o) {
		Double min = i.getMinimumAmount();
		Double mode = new ExchangeExtension(i).getMostLikelyValue();
		Double max = i.getMaximumAmount();
		if (min == null || mode == null || max == null)
			return;
		o.uncertainty = Uncertainty.triangle(min, mode, max);
	}

	private void mapTriangular(Parameter i,	org.openlca.core.model.Parameter o) {
		Double min = i.getMin();
		Double mean = i.getMean();
		Double max = i.getMax();
		if (min == null || mean == null || max == null)
			return;
		o.uncertainty = Uncertainty.triangle(min, mean, max);
	}

	private void mapUniform(Exchange i, org.openlca.core.model.Exchange o) {
		Double min = i.getMinimumAmount();
		Double max = i.getMaximumAmount();
		if (min == null || max == null)
			return;
		o.uncertainty = Uncertainty.uniform(min, max);
	}

	private void mapUniform(Parameter i, org.openlca.core.model.Parameter o) {
		Double min = i.getMin();
		Double max = i.getMax();
		if (min == null || max == null)
			return;
		o.uncertainty = Uncertainty.uniform(min, max);
	}

	private double getAmount(Exchange i) {
		var ext = new ExchangeExtension(i);
		Double val = ext.getAmount();
		if (val != null)
			return val;
		val = i.getResultingAmount();
		if (val != null)
			return val;
		return i.getMeanAmount();
	}

}
