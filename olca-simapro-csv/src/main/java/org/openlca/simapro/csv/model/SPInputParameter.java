package org.openlca.simapro.csv.model;

public class SPInputParameter extends SPParameter {

	private SPUncertainty distribution;
	private double value = 0;
	private boolean hidden = false;

	public SPUncertainty getDistribution() {
		return distribution;
	}

	public void setDistribution(SPUncertainty distribution) {
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
