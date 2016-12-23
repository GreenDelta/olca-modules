package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Classification {

	@XmlAttribute(name = "classificationId")
	public String id;

	@XmlElement(name = "classificationSystem")
	public String system;

	@XmlElement(name = "classificationValue")
	public String value;

}
