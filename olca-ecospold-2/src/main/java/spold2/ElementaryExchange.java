package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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

}
