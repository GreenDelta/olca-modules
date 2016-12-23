package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

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
