package org.openlca.ecospold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.jdom2.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class Representativeness {

	@XmlAttribute
	public Double percent;

	@XmlAttribute
	public String systemModelId;

	public String systemModelName;

	public String samplingProcedure;

	public String extrapolations;

	static Representativeness fromXml(Element e) {
		if (e == null)
			return null;
		Representativeness representativeness = new Representativeness();
		String percentStr = e.getAttributeValue("percent");
		if (percentStr != null)
			representativeness.percent = In.decimal(percentStr);
		representativeness.systemModelId = e.getAttributeValue("systemModelId");
		representativeness.systemModelName = In.childText(e, "systemModelName");
		representativeness.samplingProcedure = In.childText(e,
				"samplingProcedure");
		representativeness.extrapolations = In.childText(e, "extrapolations");
		return representativeness;
	}

	Element toXml() {
		Element element = new Element("representativeness", IO.NS);
		if (percent != null)
			element.setAttribute("percent", Double.toString(percent));
		if (systemModelId != null)
			element.setAttribute("systemModelId", systemModelId);
		if (systemModelName != null)
			Out.addChild(element, "systemModelName", systemModelName);
		if (samplingProcedure != null)
			Out.addChild(element, "samplingProcedure", samplingProcedure);
		if (extrapolations != null)
			Out.addChild(element, "extrapolations", extrapolations);
		return element;
	}

}
