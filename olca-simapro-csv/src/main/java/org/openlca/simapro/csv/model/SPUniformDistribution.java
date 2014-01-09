package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.types.DistributionParameterType;
import org.openlca.simapro.csv.model.types.DistributionType;


/**
 * Simple implementation of {@link IDistribution} for uniform distribution
 */
public class SPUniformDistribution implements IDistribution {

	private double minimum = 0;
	private double maximum = 0;
	
	public SPUniformDistribution(double minimum, double maximum) {
		this.minimum = minimum;
		this.maximum = maximum;
	}

	@Override
	public double getDistributionParameter(DistributionParameterType type) {
		double parameter = 0;
		if (type == DistributionParameterType.MINIMUM) {
			parameter = minimum;
		} else if (type == DistributionParameterType.MAXIMUM ) {
			parameter = maximum;
		}
		return parameter;
	}

	@Override
	public DistributionType getType() {
		return DistributionType.UNIFORM;
	}

	public void setMaximum(double maximum) {
		this.maximum = maximum;
	}

	public void setMinimum(double minimum) {
		this.minimum = minimum;
	}
}
