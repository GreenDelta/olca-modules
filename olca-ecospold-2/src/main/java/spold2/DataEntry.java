package spold2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class DataEntry {

	@XmlAttribute
	public String personId;

	@XmlAttribute
	public Boolean isActiveAuthor;

	@XmlAttribute
	public String personName;

	@XmlAttribute
	public String personEmail;

}
