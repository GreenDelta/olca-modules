package org.openlca.ecospold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.jdom2.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class Normal {

	@XmlAttribute
	public double meanValue;

	@XmlAttribute
	public double variance;

	@XmlAttribute
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
