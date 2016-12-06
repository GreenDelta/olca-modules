package org.openlca.ecospold2;

import org.jdom2.Element;

public class UndefinedUncertainty {

	public double minValue;
	public double maxValue;
	public double standardDeviation95;

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
