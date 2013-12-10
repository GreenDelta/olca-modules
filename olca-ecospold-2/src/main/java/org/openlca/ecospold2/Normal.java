package org.openlca.ecospold2;

import org.jdom2.Element;

public class Normal {

	private double meanValue;
	private double variance;
	private double varianceWithPedigreeUncertainty;

	public double getMeanValue() {
		return meanValue;
	}

	public void setMeanValue(double meanValue) {
		this.meanValue = meanValue;
	}

	public double getVariance() {
		return variance;
	}

	public void setVariance(double variance) {
		this.variance = variance;
	}

	public double getVarianceWithPedigreeUncertainty() {
		return varianceWithPedigreeUncertainty;
	}

	public void setVarianceWithPedigreeUncertainty(
			double varianceWithPedigreeUncertainty) {
		this.varianceWithPedigreeUncertainty = varianceWithPedigreeUncertainty;
	}

	static Normal fromXml(Element e) {
		if (e == null)
			return null;
		Normal normal = new Normal();
		normal.meanValue = In.decimal(e.getAttributeValue("meanValue"));
		normal.variance = In.decimal(e.getAttributeValue("variance"));
		normal.varianceWithPedigreeUncertainty = In.decimal(e
				.getAttributeValue("varianceWithPedigreeUncertainty"));
		return normal;
	}

	Element toXml() {
		Element element = new Element("normal", IO.NS);
		element.setAttribute("meanValue", Double.toString(meanValue));
		element.setAttribute("variance", Double.toString(variance));
		element.setAttribute("varianceWithPedigreeUncertainty",
				Double.toString(varianceWithPedigreeUncertainty));
		return element;
	}

}
