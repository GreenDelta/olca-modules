package org.openlca.simapro.csv.model;

public class SPUniformDistribution {

	private double minimum = 0;
	private double maximum = 0;

	public SPUniformDistribution(double minimum, double maximum) {
		this.minimum = minimum;
		this.maximum = maximum;
	}

	public void setMaximum(double maximum) {
		this.maximum = maximum;
	}

	public void setMinimum(double minimum) {
		this.minimum = minimum;
	}
}
