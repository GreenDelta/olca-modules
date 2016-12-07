package org.openlca.ecospold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.jdom2.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class Triangular {

	@XmlAttribute
	public double minValue;

	@XmlAttribute
	public double mostLikelyValue;

	@XmlAttribute
	public double maxValue;

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
		Element element = new Element("triangular", IO.NS);
		element.setAttribute("maxValue", Double.toString(maxValue));
		element.setAttribute("mostLikelyValue",
				Double.toString(mostLikelyValue));
		element.setAttribute("minValue", Double.toString(minValue));
		return element;
	}

}
