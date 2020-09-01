package org.openlca.core.results.solutions;

public interface SolutionProvider {

	double[] scalingVector();

	/**
	 * Get the direct requirements for product j in the product system. This is
	 * equivalent with the jth column of the technology matrix $A[:,j].
	 */
	double[] directRequirements(int product);

	/**
	 * Get the scaling vector of the product system for one unit of output (input)
	 * of product (waste flow) j. This is equivalent with the jth column of the
	 * inverse technology matrix $A^{-1}[:,j]$ in case of full in-memory matrices.
	 */
	double[] solution(int product);

	boolean hasIntensities();

	double[] intensities(int product);

	double intensity(int flow, int product);

	boolean hasImpacts();

	double[] impacts(int product);

	double impact(int indicator, int product);

	boolean hasCosts();

	double costs(int product);

	double getLoopFactor(int product);
}
