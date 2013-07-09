package org.openlca.ecospold2;

import org.jdom2.Element;

public class LogNormal {

	private double meanValue;
	private double mu;
	private double variance;
	private double varianceWithPedigreeUncertainty;

	public double getMeanValue() {
		return meanValue;
	}

	public void setMeanValue(double meanValue) {
		this.meanValue = meanValue;
	}

	public double getMu() {
		return mu;
	}

	public void setMu(double mu) {
		this.mu = mu;
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

	static LogNormal fromXml(Element e) {
		if (e == null)
			return null;
		LogNormal normal = new LogNormal();
		normal.meanValue = In.decimal(e.getAttributeValue("meanValue"));
		normal.mu = In.decimal(e.getAttributeValue("mu"));
		normal.variance = In.decimal(e.getAttributeValue("variance"));
		normal.varianceWithPedigreeUncertainty = In.decimal(e
				.getAttributeValue("varianceWithPedigreeUncertainty"));
		return normal;
	}

	Element toXml() {
		Element element = new Element("lognormal", Out.NS);
		element.setAttribute("meanValue", Double.toString(meanValue));
		element.setAttribute("mu", Double.toString(mu));
		element.setAttribute("variance", Double.toString(variance));
		element.setAttribute("varianceWithPedigreeUncertainty",
				Double.toString(varianceWithPedigreeUncertainty));
		return element;
	}

}
