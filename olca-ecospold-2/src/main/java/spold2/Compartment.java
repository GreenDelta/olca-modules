package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Compartment {

	@XmlAttribute(name = "subcompartmentId")
	public String id;

	@XmlElement
	public String compartment;

	@XmlElement(name = "subcompartment")
	public String subCompartment;

}
