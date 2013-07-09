package org.openlca.ecospold2;

import java.util.Date;
import java.util.List;

import org.jdom2.Element;

public class TimePeriod {

	private Date startDate;
	private Date endDate;
	private boolean dataValid;
	private String comment;

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public boolean isDataValid() {
		return dataValid;
	}

	public void setDataValid(boolean dataValid) {
		this.dataValid = dataValid;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	static TimePeriod fromXml(Element e) {
		if (e == null)
			return null;
		TimePeriod timePeriod = new TimePeriod();
		String startString = e.getAttributeValue("startDate");
		timePeriod.startDate = In.date(startString, "yyyy-MM-dd");
		String endString = e.getAttributeValue("endDate");
		timePeriod.endDate = In.date(endString, "yyyy-MM-dd");
		timePeriod.dataValid = In.bool(e
				.getAttributeValue("isDataValidForEntirePeriod"));
		List<Element> elements = In.childs(e, "comment", "text");
		timePeriod.comment = In.joinText(elements);
		return timePeriod;
	}

	Element toXml() {
		Element element = new Element("timePeriod", Out.NS);
		if (startDate != null)
			element.setAttribute("startDate", Out.date(startDate, "yyyy-MM-dd"));
		if (endDate != null)
			element.setAttribute("endDate", Out.date(endDate, "yyyy-MM-dd"));
		element.setAttribute("isDataValidForEntirePeriod",
				Boolean.toString(dataValid));
		if (comment != null) {
			Element commentElement = Out.addChild(element, "comment");
			Out.addIndexedText(commentElement, comment);
		}
		return element;
	}

}
