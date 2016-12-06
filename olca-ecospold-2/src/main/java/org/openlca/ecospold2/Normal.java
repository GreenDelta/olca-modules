package org.openlca.ecospold2;

import org.jdom2.Element;

public class Normal {

	public double meanValue;
	public double variance;
	public double varianceWithPedigreeUncertainty;

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
