package org.openlca.ecospold2;

import org.jdom2.Element;

public class Triangular {

	private double minValue;
	private double mostLikelyValue;
	private double maxValue;

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public double getMostLikelyValue() {
		return mostLikelyValue;
	}

	public void setMostLikelyValue(double mostLikelyValue) {
		this.mostLikelyValue = mostLikelyValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	static Triangular fromXml(Element e) {
		if (e == null)
			return null;
		Triangular triangular = new Triangular();
		triangular.maxValue = In.decimal(e.getAttributeValue("maxValue"));
		triangular.mostLikelyValue = In.decimal(e
				.getAttributeValue("mostLikelyValue"));
		triangular.minValue = In.decimal(e.getAttributeValue("minValue"));
		return triangular;
	}

	Element toXml() {
		Element element = new Element("triangular");
		element.setAttribute("maxValue", Double.toString(maxValue));
		element.setAttribute("mostLikelyValue",
				Double.toString(mostLikelyValue));
		element.setAttribute("minValue", Double.toString(minValue));
		return element;
	}

}
