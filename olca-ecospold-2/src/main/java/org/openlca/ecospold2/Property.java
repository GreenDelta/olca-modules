package org.openlca.ecospold2;

import org.jdom2.Element;

public class Property {

	public String id;
	public double amount;
	public String unitId;
	public String name;
	public String unitName;
	public String variableName;
	public String mathematicalRelation;
	public boolean isDefiningValue;

	static Property fromXml(Element e) {
		if (e == null)
			return null;
		Property p = new Property();
		p.amount = In.decimal(e.getAttributeValue("amount"));
		p.id = e.getAttributeValue("propertyId");
		p.name = In.childText(e, "name");
		p.unitId = e.getAttributeValue("unitId");
		p.unitName = In.childText(e, "unitName");
		p.mathematicalRelation = e.getAttributeValue("mathematicalRelation");
		p.variableName = e.getAttributeValue("variableName");
		Boolean isDefiningValue = In.optionalBool("isDefiningValue");
		if (isDefiningValue != null && isDefiningValue)
			p.isDefiningValue = true;
		return p;
	}

	Element toXml() {
		Element e = new Element("property", IO.NS);
		e.setAttribute("amount", Double.toString(amount));
		e.setAttribute("propertyId", id);
		if (unitId != null)
			e.setAttribute("unitId", unitId);
		if (mathematicalRelation != null)
			e.setAttribute("mathematicalRelation", mathematicalRelation);
		if (variableName != null)
			e.setAttribute("variableName", variableName);
		if (isDefiningValue)
			e.setAttribute("isDefiningValue", "true");
		Out.addChild(e, "name", name);
		Out.addChild(e, "unitName", unitName);
		return e;
	}

}
