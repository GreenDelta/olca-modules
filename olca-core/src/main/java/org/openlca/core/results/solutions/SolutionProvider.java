package org.openlca.core.results.solutions;

public interface SolutionProvider {

	double[] solution(int i);

	boolean hasIntensities();

	double[] intensities(int i);

	boolean hasImpacts();

	double[] impacts(int i);

	boolean hasCosts();

	double costs(int i);

	double getLoopFactor(int i);
}
