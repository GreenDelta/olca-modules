package org.openlca.simapro.csv.model.enums;

public enum DistributionType {

	LOG_NORMAL("Lognormal",
			new DistributionParameterType[]{DistributionParameterType.SQUARED_STANDARD_DEVIATION}),

	NORMAL("Normal",
			new DistributionParameterType[]{DistributionParameterType.DOUBLED_STANDARD_DEVIATION}),

	TRIANGLE("Triangle", new DistributionParameterType[]{
			DistributionParameterType.MINIMUM,
			DistributionParameterType.MAXIMUM}),

	UNDEFINED("Undefined", new DistributionParameterType[0]),

	UNIFORM("Uniform", new DistributionParameterType[]{
			DistributionParameterType.MINIMUM,
			DistributionParameterType.MAXIMUM});

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
