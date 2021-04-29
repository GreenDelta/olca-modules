package spold2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Company {

	@XmlAttribute
	public String id;

	@XmlAttribute
	public String code;

	@XmlAttribute
	public String website;

	@XmlElement
	public String name;

	@XmlElement
	public String comment;

}
