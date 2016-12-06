package org.openlca.ecospold2;

import java.util.Date;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class ActivityIndexEntry {

	public String id;
	public String activityNameId;
	public String geographyId;
	public Date startDate;
	public Date endDate;
	public String systemModelId;

	static ActivityIndexEntry fromXml(Element e) {
		if (e == null)
			return null;
		ActivityIndexEntry activityindexentry = new ActivityIndexEntry();
		activityindexentry.id = e.getAttributeValue("id");
		activityindexentry.activityNameId = e
				.getAttributeValue("activityNameId");
		activityindexentry.geographyId = e.getAttributeValue("geographyId");
		activityindexentry.startDate = In.date(
				e.getAttributeValue("startDate"), IO.XML_DATE_TIME);
		activityindexentry.endDate = In.date(e.getAttributeValue("endDate"),
				IO.XML_DATE_TIME);
		activityindexentry.systemModelId = e.getAttributeValue("systemModelId");
		return activityindexentry;
	}

	Element toXml(Namespace ns) {
		Element element = new Element("activityIndexEntry", ns);

		if (id != null)
			element.setAttribute("id", id);

		if (activityNameId != null)
			element.setAttribute("activityNameId", activityNameId);

		if (geographyId != null)
			element.setAttribute("geographyId", geographyId);

		if (startDate != null)
			element.setAttribute("startDate",
					Out.date(startDate, IO.XML_DATE_TIME));

		if (endDate != null)
			element.setAttribute("endDate", Out.date(endDate, IO.XML_DATE_TIME));

		if (systemModelId != null)
			element.setAttribute("systemModelId", systemModelId);

		return element;
	}

}
