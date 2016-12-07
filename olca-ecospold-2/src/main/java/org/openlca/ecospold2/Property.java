package org.openlca.ecospold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.jdom2.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class Property {

	@XmlAttribute(name = "propertyId")
	public String id;

	@XmlAttribute
	public String variableName;

	@XmlAttribute
	public double amount;

	@XmlAttribute
	public boolean isDefiningValue;

	@XmlAttribute
	public String mathematicalRelation;

	@XmlAttribute
	public String unitId;

	@XmlElement
	public String name;

	@XmlElement(name = "unitName")
	public String unit;

	static Property fromXml(Element e) {
		if (e == null)
			return null;
		Property p = new Property();
		p.amount = In.decimal(e.getAttributeValue("amount"));
		p.id = e.getAttributeValue("propertyId");
		p.name = In.childText(e, "name");
		p.unitId = e.getAttributeValue("unitId");
		p.unit = In.childText(e, "unitName");
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
		Out.addChild(e, "unitName", unit);
		return e;
	}

}
