package spold2;

import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class ActivityIndexEntry {

	@XmlAttribute
	public String id;

	@XmlAttribute
	public String activityNameId;

	@XmlAttribute
	public String geographyId;

	@XmlAttribute
	public Date startDate;

	@XmlAttribute
	public Date endDate;

	@XmlAttribute
	public String systemModelId;

}
