package spold2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Person {

	@XmlAttribute
	public String id;

	@XmlAttribute
	public String name;

	@XmlAttribute
	public String address;

	@XmlAttribute
	public String telephone;

	@XmlAttribute
	public String telefax;

	@XmlAttribute
	public String email;

	@XmlAttribute
	public String companyId;

	@XmlElement(name = "companyName")
	public String company;

}
