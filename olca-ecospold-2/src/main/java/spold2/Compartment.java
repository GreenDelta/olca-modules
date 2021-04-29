package spold2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Compartment {

	@XmlAttribute(name = "subcompartmentId")
	public String id;

	@XmlElement
	public String compartment;

	@XmlElement(name = "subcompartment")
	public String subCompartment;

}
