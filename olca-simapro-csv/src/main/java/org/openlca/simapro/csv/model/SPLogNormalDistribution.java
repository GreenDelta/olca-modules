package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.enums.DistributionParameterType;
import org.openlca.simapro.csv.model.enums.DistributionType;

/**
 * Simple implementation of {@link IDistribution} for logarithmic normal
 * distribution
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public class SPLogNormalDistribution implements IDistribution {

	private double squaredStandardDeviation = 0;
	private SPPedigreeMatrix pedigreeMatrix;

	public SPLogNormalDistribution(double squaredStandardDeviation,
			SPPedigreeMatrix pedigreeMatrix) {
		this.squaredStandardDeviation = squaredStandardDeviation;
		this.pedigreeMatrix = pedigreeMatrix;
	}

	@Override
	public double getDistributionParameter(DistributionParameterType type) {
		double parameter = 0;
		if (type == DistributionParameterType.SQUARED_STANDARD_DEVIATION) {
			parameter = squaredStandardDeviation;
		}
		return parameter;
	}

	public SPPedigreeMatrix getPedigreeMatrix() {
		return pedigreeMatrix;
	}

	public void setPedigreeMatrix(SPPedigreeMatrix pedigreeMatrix) {
		this.pedigreeMatrix = pedigreeMatrix;
	}

	@Override
	public DistributionType getType() {
		return DistributionType.LOG_NORMAL;
	}

	public void setSquaredStandardDeviation(double squaredStandardDeviation) {
		this.squaredStandardDeviation = squaredStandardDeviation;
	}

}
