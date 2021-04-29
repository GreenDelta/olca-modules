package spold2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Geography {

	@XmlAttribute(name = "geographyId")
	public String id;

	@XmlElement(name = "shortname")
	public String shortName;

	public RichText comment;

}
