package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.types.DistributionParameterType;
import org.openlca.simapro.csv.model.types.DistributionType;

/**
 * Simple implementation of {@link IDistribution} for normal distribution
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public class SPNormalDistribution implements IDistribution {

	/**
	 * The doubled standard deviation
	 */
	private double doubledStandardDeviation = 0;

	/**
	 * Creates a new normal distribution
	 * 
	 * @param doubledStandardDeviation
	 *            The double standard deviation
	 */
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

	/**
	 * Setter of the doubled standard deviation
	 * 
	 * @param doubledStandardDeviation
	 *            The new doubled standard deviation
	 */
	public void setDoubledStandardDeviation(double doubledStandardDeviation) {
		this.doubledStandardDeviation = doubledStandardDeviation;
	}

}
