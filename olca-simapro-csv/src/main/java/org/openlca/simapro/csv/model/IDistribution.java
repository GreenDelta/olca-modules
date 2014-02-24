package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.enums.DistributionParameterType;
import org.openlca.simapro.csv.model.enums.DistributionType;

/**
 * This class represents a SimaPro uncertainty distribution
<<<<<<< Updated upstream
=======
 * 
 * 
>>>>>>> Stashed changes
 */
public interface IDistribution {

	/**
	 * Getter of the distribution parameter for the given type
	 * 
	 * @param type
	 *            The type of distribution parameter which is requested
	 * @return The value of the distribution parameter with the given type, if
	 *         defined in the distribution, null otherwise
	 */
	double getDistributionParameter(
			DistributionParameterType type);

	/**
	 * Getter of the distribution type
	 * 
	 * @see DistributionType
	 * @return The type of the distribution
	 */
	DistributionType getType();

}
