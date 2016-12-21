package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Property {

	@XmlAttribute(name = "propertyId")
	public String id;

	/** ID field in case the property is from a master data file. */
	@XmlAttribute(name = "id")
	public String masterId;

	@XmlAttribute
	public String variableName;

	/** Only valid for properties in master data files. */
	@XmlAttribute
	public String defaultVariableName;

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
