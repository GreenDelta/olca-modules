package org.openlca.io.simapro.csv.input;

import org.openlca.simapro.csv.model.Uncertainty;
import org.openlca.simapro.csv.model.enums.DistributionParameter;

final class Uncertainties {

	private Uncertainties() {
	}

	public static org.openlca.core.model.Uncertainty get(double mean,
			Uncertainty uncertainty) {
		if (uncertainty == null || uncertainty.getType() == null)
			return null;
		switch (uncertainty.getType()) {
		case LOG_NORMAL:
			return logNormal(mean, uncertainty);
		case NORMAL:
			return normal(mean, uncertainty);
		case TRIANGLE:
			return triangle(mean, uncertainty);
		case UNIFORM:
			return uniform(uncertainty);
		default:
			return null;
		}
	}

	private static org.openlca.core.model.Uncertainty logNormal(double mean,
			Uncertainty uncertainty) {
		return org.openlca.core.model.Uncertainty.logNormal(
				mean,
				Math.sqrt(uncertainty
						.getParameterValue(DistributionParameter.SQUARED_SD))
				);
	}

	private static org.openlca.core.model.Uncertainty normal(double mean,
			Uncertainty uncertainty) {
		return org.openlca.core.model.Uncertainty.normal(
				mean,
				0.5 * uncertainty
						.getParameterValue(DistributionParameter.DOUBLED_SD)
				);
	}

	private static org.openlca.core.model.Uncertainty triangle(double mean,
			Uncertainty uncertainty) {
		return org.openlca.core.model.Uncertainty.triangle(
				uncertainty.getParameterValue(DistributionParameter.MINIMUM),
				mean,
				uncertainty.getParameterValue(DistributionParameter.MAXIMUM)
				);
	}

	private static org.openlca.core.model.Uncertainty uniform(
			Uncertainty uncertainty) {
		return org.openlca.core.model.Uncertainty.uniform(
				uncertainty.getParameterValue(DistributionParameter.MINIMUM),
				uncertainty.getParameterValue(DistributionParameter.MAXIMUM)
				);
	}
}
