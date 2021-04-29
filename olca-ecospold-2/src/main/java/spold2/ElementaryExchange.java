package spold2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

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
