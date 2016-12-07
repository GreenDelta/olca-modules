package org.openlca.ecospold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.jdom2.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class LogNormal {

	@XmlAttribute
	public double meanValue;

	@XmlAttribute
	public double mu;

	@XmlAttribute
	public double variance;

	@XmlAttribute
	public double varianceWithPedigreeUncertainty;

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
		Element element = new Element("lognormal", IO.NS);
		element.setAttribute("meanValue", Double.toString(meanValue));
		element.setAttribute("mu", Double.toString(mu));
		element.setAttribute("variance", Double.toString(variance));
		element.setAttribute("varianceWithPedigreeUncertainty",
				Double.toString(varianceWithPedigreeUncertainty));
		return element;
	}

}
