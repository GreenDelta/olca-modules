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

	public void map(Exchange o, org.openlca.ilcd.processes.Exchange i) {
		Uncertainty uncertainty = o.uncertainty;
		if (uncertainty == null
				|| uncertainty.distributionType == UncertaintyType.NONE)
			return;
		switch (uncertainty.distributionType) {
			case LOG_NORMAL:
				mapLogNormal(o, i);
				break;
			case NORMAL:
				mapNormal(o, i);
				break;
			case TRIANGLE:
				mapTriangular(o, i);
				break;
			case UNIFORM:
				mapUniform(o, i);
				break;
			default:
				break;
		}
	}

	public void map(Parameter o, org.openlca.ilcd.processes.Parameter i) {
		var uncertainty = o.uncertainty;
		if (uncertainty == null
				|| uncertainty.distributionType == UncertaintyType.NONE)
			return;
		switch (uncertainty.distributionType) {
			case LOG_NORMAL:
				mapLogNormal(o, i);
				break;
			case NORMAL:
				mapNormal(o, i);
				break;
			case TRIANGLE:
				mapTriangle(o, i);
				break;
			case UNIFORM:
				mapUniform(o, i);
				break;
			default:
				break;
		}
	}

	private void mapLogNormal(Exchange o, org.openlca.ilcd.processes.Exchange i) {
		var param = getUncertaintyParam(2, o);
		if (param == null)
			return;
		i.withRelativeStandardDeviation95In(param);
		i.withUncertaintyDistribution(UncertaintyDistribution.LOG_NORMAL);
	}

	private void mapLogNormal(Parameter o, org.openlca.ilcd.processes.Parameter i) {
		Double std = o.uncertainty.parameter2;
		if (std == null)
			return;
		i.withDispersion(std);
		i.withDistribution(UncertaintyDistribution.LOG_NORMAL);
	}

	private void mapNormal(Exchange o, org.openlca.ilcd.processes.Exchange i) {
		Double param = getUncertaintyParam(2, o);
		if (param == null)
			return;
		i.withRelativeStandardDeviation95In(param);
		i.withUncertaintyDistribution(UncertaintyDistribution.NORMAL);
	}

	private void mapNormal(Parameter o, org.openlca.ilcd.processes.Parameter i) {
		Double std = o.uncertainty.parameter2;
		if (std == null)
			return;
		i.withDispersion(std);
		i.withDistribution(UncertaintyDistribution.NORMAL);
	}

	private void mapTriangular(Exchange o, org.openlca.ilcd.processes.Exchange i) {
		Double param1 = getUncertaintyParam(1, o);
		Double param2 = getUncertaintyParam(2, o);
		Double param3 = getUncertaintyParam(3, o);
		if (param1 == null || param2 == null || param3 == null)
			return;
		i.withMinimumAmount(param1);
		new ExchangeExtension(i).setMostLikelyValue(param2);
		i.withMaximumAmount(param3);
		i.withUncertaintyDistribution(UncertaintyDistribution.TRIANGULAR);
	}

	private void mapTriangle(Parameter o, org.openlca.ilcd.processes.Parameter i) {
		Double min = o.uncertainty.parameter1;
		// Double mode = o.getUncertainty().getParameter2Value();
		// TODO: ILCD do not provide a field for the mode, we have to add
		// an extension to the format
		Double max = o.uncertainty.parameter3;
		if (min == null || max == null)
			return;
		i.withMin(min);
		i.withMax(max);
		i.withDistribution(UncertaintyDistribution.TRIANGULAR);
	}

	private void mapUniform(Exchange o,
			org.openlca.ilcd.processes.Exchange i) {
		Double param1 = getUncertaintyParam(1, o);
		Double param2 = getUncertaintyParam(2, o);
		if (param1 == null || param2 == null)
			return;
		i.withMinimumAmount(param1);
		i.withMaximumAmount(param2);
		i.withUncertaintyDistribution(UncertaintyDistribution.UNIFORM);
	}

	private void mapUniform(Parameter o,
			org.openlca.ilcd.processes.Parameter i) {
		Double min = o.uncertainty.parameter1;
		Double max = o.uncertainty.parameter2;
		if (min == null || max == null)
			return;
		i.withMin(min);
		i.withMax(max);
		i.withDistribution(UncertaintyDistribution.UNIFORM);
	}

	private Double getUncertaintyParam(int param, Exchange o) {
		var uncertainty = o.uncertainty;
		return switch (param) {
			case 1 -> uncertainty.parameter1;
			case 2 -> uncertainty.parameter2;
			case 3 -> uncertainty.parameter3;
			default -> null;
		};
	}
}
