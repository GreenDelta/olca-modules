package org.openlca.ecospold2;

import java.util.Date;
import java.util.List;

import org.jdom2.Element;

public class TimePeriod {

	public Date startDate;
	public Date endDate;
	public boolean dataValid;
	public String comment;

	static TimePeriod fromXml(Element e) {
		if (e == null)
			return null;
		TimePeriod timePeriod = new TimePeriod();
		String startString = e.getAttributeValue("startDate");
		timePeriod.startDate = In.date(startString, IO.XML_DATE);
		String endString = e.getAttributeValue("endDate");
		timePeriod.endDate = In.date(endString, IO.XML_DATE);
		timePeriod.dataValid = In.bool(e
				.getAttributeValue("isDataValidForEntirePeriod"));
		List<Element> elements = In.childs(e, "comment", "text");
		timePeriod.comment = In.joinText(elements);
		return timePeriod;
	}

	Element toXml() {
		Element element = new Element("timePeriod", IO.NS);
		if (startDate != null)
			element.setAttribute("startDate", Out.date(startDate, IO.XML_DATE));
		if (endDate != null)
			element.setAttribute("endDate", Out.date(endDate, IO.XML_DATE));
		element.setAttribute("isDataValidForEntirePeriod",
				Boolean.toString(dataValid));
		if (comment != null) {
			Element commentElement = Out.addChild(element, "comment");
			Out.addIndexedText(commentElement, comment);
		}
		return element;
	}

}
