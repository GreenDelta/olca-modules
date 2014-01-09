package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.types.DistributionParameterType;
import org.openlca.simapro.csv.model.types.DistributionType;

/**
 * Simple implementation of {@link IDistribution} for normal distribution
 */
public class SPNormalDistribution implements IDistribution {

	private double doubledStandardDeviation = 0;

	public SPNormalDistribution(double doubledStandardDeviation) {
		this.doubledStandardDeviation = doubledStandardDeviation;
	}

	@Override
	public double getDistributionParameter(DistributionParameterType type) {
		double parameter = 0;
		if (type == DistributionParameterType.DOUBLED_STANDARD_DEVIATION) {
			parameter = doubledStandardDeviation;
		}
		return parameter;
	}

	@Override
	public DistributionType getType() {
		return DistributionType.NORMAL;
	}

	public void setDoubledStandardDeviation(double doubledStandardDeviation) {
		this.doubledStandardDeviation = doubledStandardDeviation;
	}

}
