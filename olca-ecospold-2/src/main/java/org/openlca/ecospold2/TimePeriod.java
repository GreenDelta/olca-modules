package org.openlca.ecospold2;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.jdom2.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class TimePeriod {

	@XmlAttribute(name = "startDate")
	public Date start;

	@XmlAttribute(name = "endDate")
	public Date end;

	@XmlAttribute(name = "isDataValidForEntirePeriod")
	public boolean dataValid;

	public RichText comment;

	static TimePeriod fromXml(Element e) {
		if (e == null)
			return null;
		TimePeriod timePeriod = new TimePeriod();
		String startString = e.getAttributeValue("startDate");
		timePeriod.start = In.date(startString, IO.XML_DATE);
		String endString = e.getAttributeValue("endDate");
		timePeriod.end = In.date(endString, IO.XML_DATE);
		timePeriod.dataValid = In.bool(e
				.getAttributeValue("isDataValidForEntirePeriod"));
		timePeriod.comment = In.richText(e, "comment");
		return timePeriod;
	}

	Element toXml() {
		Element e = new Element("timePeriod", IO.NS);
		if (start != null)
			e.setAttribute("startDate", Out.date(start, IO.XML_DATE));
		if (end != null)
			e.setAttribute("endDate", Out.date(end, IO.XML_DATE));
		e.setAttribute("isDataValidForEntirePeriod",
				Boolean.toString(dataValid));
		if (comment != null) {
			Element commentElement = Out.addChild(e, "comment");
			Out.fill(commentElement, comment);
		}
		return e;
	}

}
