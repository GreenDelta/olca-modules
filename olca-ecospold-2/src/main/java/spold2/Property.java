package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

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

}
