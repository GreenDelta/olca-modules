package org.openlca.simapro.csv.model;

public class SPInputParameter extends SPParameter {

	private IDistribution distribution;
	private double value = 0;
	private boolean hidden = false;

	public IDistribution getDistribution() {
		return distribution;
	}

	public void setDistribution(IDistribution distribution) {
		this.distribution = distribution;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

}
