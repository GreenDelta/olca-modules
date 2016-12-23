package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

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
