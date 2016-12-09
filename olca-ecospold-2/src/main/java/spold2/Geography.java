package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Geography {

	@XmlAttribute(name = "geographyId")
	public String id;

	@XmlElement(name = "shortname")
	public String shortName;

	public RichText comment;

}
