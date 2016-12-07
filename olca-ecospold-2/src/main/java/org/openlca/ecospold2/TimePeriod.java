package org.openlca.ecospold2;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class TimePeriod {

	@XmlAttribute(name = "startDate")
	public Date start;

	@XmlAttribute(name = "endDate")
	public Date end;

	@XmlAttribute(name = "isDataValidForEntirePeriod")
	public boolean dataValid;

	public RichText comment;

}
