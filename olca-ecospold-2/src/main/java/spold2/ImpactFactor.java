package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

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
