package org.openlca.ecospold2;

import org.jdom2.Element;

public class MacroEconomicScenario {

	private String id;
	private String name;
	private String comment;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	static MacroEconomicScenario fromXml(Element e) {
		if (e == null)
			return null;
		MacroEconomicScenario scenario = new MacroEconomicScenario();
		scenario.setId(e.getAttributeValue("macroEconomicScenarioId"));
		scenario.setName(In.childText(e, "name"));
		scenario.setComment(In.childText(e, "comment"));
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
