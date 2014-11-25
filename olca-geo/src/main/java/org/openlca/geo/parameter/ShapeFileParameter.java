package org.openlca.geo.parameter;

/**
 * A numeric attribute of features in a shapefile.
 */
class ShapeFileParameter {

	private String name;
	private double min;
	private double max;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}
}
