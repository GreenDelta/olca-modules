package org.openlca.ecospold2;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class ElementaryExchange extends Exchange {

	public String elementaryExchangeId;
	public Compartment compartment;
	public String formula;

	static ElementaryExchange fromXml(Element e) {
		if (e == null)
			return null;
		ElementaryExchange ee = new ElementaryExchange();
		ee.elementaryExchangeId = e.getAttributeValue("elementaryExchangeId");
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
		if (elementaryExchangeId != null)
			e.setAttribute("elementaryExchangeId", elementaryExchangeId);
		if (compartment != null)
			e.addContent(compartment.toXml());
		if (formula != null)
			e.setAttribute("formula", formula);
		writeInputOutputGroup(e);
		return e;
	}

}
