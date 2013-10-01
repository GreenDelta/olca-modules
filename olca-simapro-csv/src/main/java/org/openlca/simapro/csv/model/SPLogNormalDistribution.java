package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.types.DistributionParameterType;
import org.openlca.simapro.csv.model.types.DistributionType;

/**
 * Simple implementation of {@link IDistribution} for logarithmic normal
 * distribution
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public class SPLogNormalDistribution implements IDistribution {

	/**
	 * The squared standard deviation
	 */
	private double squaredStandardDeviation = 0;

	/**
	 * The pedigree matrix of the flow
	 */
	private SPPedigreeMatrix pedigreeMatrix;

	/**
	 * Creates a new logarithmic normal distribution
	 * 
	 * @param squaredStandardDeviation
	 *            The squared standard deviation
	 */
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

	/**
	 * 
	 * @return {@link SPPedigreeMatrix}
	 */
	public SPPedigreeMatrix getPedigreeMatrix() {
		return pedigreeMatrix;
	}

	/**
	 * Set the pedigree matrix
	 * 
	 * @param pedigreeMatrix
	 */
	public void setPedigreeMatrix(SPPedigreeMatrix pedigreeMatrix) {
		this.pedigreeMatrix = pedigreeMatrix;
	}

	@Override
	public DistributionType getType() {
		return DistributionType.LOG_NORMAL;
	}

	/**
	 * Setter of the squared standard deviation
	 * 
	 * @param squaredStandardDeviation
	 *            The new squared standard deviation
	 */
	public void setSquaredStandardDeviation(double squaredStandardDeviation) {
		this.squaredStandardDeviation = squaredStandardDeviation;
	}

}
