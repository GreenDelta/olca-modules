package org.openlca.ecospold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.jdom2.Element;
import org.jdom2.Namespace;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
		"name",
		"unit",
		"comment",
		"uncertainty",
		"properties",
		"compartment",
		"inputGroup",
		"outputGroup"
})
public class ElementaryExchange extends Exchange {

	@XmlAttribute(name = "elementaryExchangeId")
	public String flowId;

	@XmlAttribute
	public String formula;

	@XmlElement
	public Compartment compartment;

	static ElementaryExchange fromXml(Element e) {
		if (e == null)
			return null;
		ElementaryExchange ee = new ElementaryExchange();
		ee.flowId = e.getAttributeValue("elementaryExchangeId");
		ee.formula = e.getAttributeValue("formula");
		ee.readValues(e);
		ee.compartment = Compartment.fromXml(In.child(e, "compartment"));
		return ee;
	}

	Element toXml() {
		return toXml(IO.NS);
	}

	Element toXml(Namespace ns) {
		Element e = new Element("elementaryExchange", ns);
		writeValues(e);
		if (flowId != null)
			e.setAttribute("elementaryExchangeId", flowId);
		if (compartment != null)
			e.addContent(compartment.toXml());
		if (formula != null)
			e.setAttribute("formula", formula);
		writeInputOutputGroup(e);
		return e;
	}

}
