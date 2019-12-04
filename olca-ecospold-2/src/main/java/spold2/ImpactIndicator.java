package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class ImpactIndicator {

	@XmlAttribute
	public String id;

	@XmlElement(name = "name")
	public String name;

	@XmlElement(name = "unitName")
	public String unit;
}
