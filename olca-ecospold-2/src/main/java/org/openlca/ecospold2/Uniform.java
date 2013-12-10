package org.openlca.ecospold2;

import org.jdom2.Element;

public class Uniform {

	private double minValue;
	private double maxValue;

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	static Uniform fromXml(Element e) {
		if (e == null)
			return null;
		Uniform uniform = new Uniform();
		uniform.maxValue = In.decimal(e.getAttributeValue("maxValue"));
		uniform.minValue = In.decimal(e.getAttributeValue("minValue"));
		return uniform;
	}

	Element toXml() {
		Element e = new Element("uniform", IO.NS);
		e.setAttribute("maxValue", Double.toString(maxValue));
		e.setAttribute("minValue", Double.toString(minValue));
		return e;
	}

}
