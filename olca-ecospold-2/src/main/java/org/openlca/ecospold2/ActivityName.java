package org.openlca.ecospold2;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class ActivityName {

	public String id;
	public String name;

	static ActivityName fromXml(Element e) {
		if (e == null)
			return null;
		ActivityName activityname = new ActivityName();
		activityname.id = e.getAttributeValue("id");
		activityname.name = In.childText(e, "name");
		return activityname;
	}

	Element toXml(Namespace ns) {
		Element element = new Element("activityName", ns);
		if (id != null)
			element.setAttribute("id", id);
		if (name != null)
			Out.addChild(element, "name", name);
		return element;
	}

}
