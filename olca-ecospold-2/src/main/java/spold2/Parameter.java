package spold2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Parameter {

	@XmlAttribute(name = "parameterId")
	public String id;

	@XmlAttribute
	public String variableName;

	@XmlAttribute
	public String mathematicalRelation;

	@XmlAttribute
	public Boolean isCalculatedAmount;

	@XmlAttribute
	public double amount;

	@XmlElement
	public String name;

	@XmlElement
	public String unitName;

	@XmlElement
	public Uncertainty uncertainty;

	@XmlElement
	public String comment;

	@XmlElement(namespace = "http://openlca.org/ecospold2-extensions")
	public String scope;

}
