package org.openlca.ecospold2;

import org.jdom2.Element;

public class UndefinedUncertainty {

	private double minValue;
	private double maxValue;
	private double standardDeviation95;

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

	public double getStandardDeviation95() {
		return standardDeviation95;
	}

	public void setStandardDeviation95(double standardDeviation95) {
		this.standardDeviation95 = standardDeviation95;
	}

	static UndefinedUncertainty fromXml(Element e) {
		if (e == null)
			return null;
		UndefinedUncertainty uncertainty = new UndefinedUncertainty();
		uncertainty.maxValue = In.decimal(e.getAttributeValue("maxValue"));
		uncertainty.minValue = In.decimal(e.getAttributeValue("minValue"));
		uncertainty.standardDeviation95 = In.decimal(e
				.getAttributeValue("standardDeviation95"));
		return uncertainty;
	}

	Element toXml() {
		Element element = new Element("undefined", IO.NS);
		element.setAttribute("maxValue", Double.toString(maxValue));
		element.setAttribute("minValue", Double.toString(minValue));
		element.setAttribute("standardDeviation95",
				Double.toString(standardDeviation95));
		return element;
	}

}
