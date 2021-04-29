package spold2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Classification {

	@XmlAttribute(name = "classificationId")
	public String id;

	@XmlElement(name = "classificationSystem")
	public String system;

	@XmlElement(name = "classificationValue")
	public String value;

}
