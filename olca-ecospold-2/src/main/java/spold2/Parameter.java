package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

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
