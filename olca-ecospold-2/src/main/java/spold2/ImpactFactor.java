package spold2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class ImpactFactor {

	@XmlAttribute(name = "elementaryExchangeId")
	public String flowID;

	@XmlAttribute(name = "amount")
	public double amount;

	@XmlElement(name = "exchangeName")
	public String flowName;

	@XmlElement(name = "compartment")
	public Compartment compartment;

	@XmlElement(name = "unitName")
	public String flowUnit;
}
