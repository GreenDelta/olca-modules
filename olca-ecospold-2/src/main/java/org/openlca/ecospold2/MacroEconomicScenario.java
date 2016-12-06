package org.openlca.ecospold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.jdom2.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class MacroEconomicScenario {

	@XmlAttribute(name = "macroEconomicScenarioId")
	public String id;

	public String name;
	public String comment;

	static MacroEconomicScenario fromXml(Element e) {
		if (e == null)
			return null;
		MacroEconomicScenario scenario = new MacroEconomicScenario();
		scenario.id = e.getAttributeValue("macroEconomicScenarioId");
		scenario.name = In.childText(e, "name");
		scenario.comment = In.childText(e, "comment");
		return scenario;
	}

	Element toXml() {
		Element e = new Element("macroEconomicScenario", IO.NS);
		e.setAttribute("macroEconomicScenarioId", id);
		Out.addChild(e, "name", name);
		if (comment != null)
			Out.addChild(e, "comment", comment);
		return e;
	}

}
