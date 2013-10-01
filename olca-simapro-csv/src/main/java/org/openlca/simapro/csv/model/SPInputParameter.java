package org.openlca.simapro.csv.model;

/**
 * This class represents a process parameter having a double value (no formula)
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public class SPInputParameter extends SPParameter {

	private IDistribution distribution;
	private double value = 0;
	private boolean hidden = false;

	public SPInputParameter(String name, double value) {
		this.name = name;
		this.value = value;
	}

	public SPInputParameter(String name, double value,
			IDistribution distribution) {
		this.name = name;
		this.value = value;
		this.distribution = distribution;
	}

	public SPInputParameter(String name, double value,
			IDistribution distribution, String comment, boolean hidden) {
		this.name = name;
		this.value = value;
		this.distribution = distribution;
		this.comment = comment;
		this.hidden = hidden;
	}

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
