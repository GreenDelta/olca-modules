package org.openlca.ecospold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.jdom2.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class Uniform {

	@XmlAttribute
	public double minValue;

	@XmlAttribute
	public double maxValue;

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
