package org.openlca.core.results.solutions;

public interface SolutionProvider {

	double[] scalingVector();

	double[] columnOfA(int product);

	double valueOfA(int row, int col);

	double scaledValueOfA(int row, int col);

	/**
	 * Get the scaling vector of the product system for one unit of output (input)
	 * of product (waste flow) j. This is equivalent with the jth column of the
	 * inverse technology matrix $A^{-1}[:,j]$ in case of full in-memory matrices.
	 */
	double[] solutionOfOne(int product);

	boolean hasFlows();

	/**
	 * Returns the total flow results (direct + upstream) related to the final
	 * demand of the product system.
	 */
	double[] totalFlows();

	/**
	 * Returns the total flow results (direct + upstream) related to one unit of
	 * the given product in the system.
	 */
	double[] totalFlowsOfOne(int product);

	/**
	 * Returns the total result (direct + upstream) of the given flow related
	 * to one unit of the given product in the system.
	 */
	double totalFlowOfOne(int flow, int product);

	boolean hasImpacts();

	double[] totalImpacts();

	double[] totalImpactsOfOne(int product);

	double totalImpactOfOne(int indicator, int product);

	boolean hasCosts();

	double totalCosts();

	double totalCostsOfOne(int product);

	double loopFactorOf(int product);
}
