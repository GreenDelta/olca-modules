package org.openlca.simapro.csv.model.types;

/**
 * Enumeration of possible uncertainty distributions
 * 
 * @author Sebastian Greve
 * 
 */
public enum DistributionType {

	/**
	 * Logarithmic normal distribution
	 */
	LOG_NORMAL(
			"Lognormal",
			new DistributionParameterType[] { DistributionParameterType.SQUARED_STANDARD_DEVIATION }),

	/**
	 * Normal distribution
	 */
	NORMAL(
			"Normal",
			new DistributionParameterType[] { DistributionParameterType.DOUBLED_STANDARD_DEVIATION }),

	/**
	 * Triangle distribution
	 */
	TRIANGLE("Triangle", new DistributionParameterType[] {
			DistributionParameterType.MINIMUM,
			DistributionParameterType.MAXIMUM }),

	/**
	 * No distribution
	 */
	UNDEFINED("Undefined", new DistributionParameterType[0]),

	/**
	 * Uniform distribution
	 */
	UNIFORM("Uniform", new DistributionParameterType[] {
			DistributionParameterType.MINIMUM,
			DistributionParameterType.MAXIMUM });

	private DistributionParameterType[] parameterTypes;
	private String value;

	private DistributionType(String value,
			DistributionParameterType[] parameterTypes) {
		this.value = value;
		this.parameterTypes = parameterTypes;
	}

	public DistributionParameterType[] getParameterTypes() {
		return parameterTypes;
	}

	public String getValue() {
		return value;
	}

}
