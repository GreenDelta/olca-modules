package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.jdom2.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class Activity {

	@XmlAttribute
	public String id;

	@XmlAttribute
	public String activityNameId;

	@XmlAttribute
	public String activityNameContextId;

	@XmlAttribute
	public String parentActivityId;

	@XmlAttribute
	public String parentActivityContextId;

	@XmlAttribute
	public Integer inheritanceDepth;

	@XmlAttribute
	public int type;

	@XmlAttribute
	public int specialActivityType;

	@XmlAttribute
	public Integer energyValues;

	@XmlAttribute
	public String masterAllocationPropertyId;

	@XmlAttribute
	public String masterAllocationPropertyContextId;

	@XmlElement(name = "activityName")
	public String name;

	@XmlElement(name = "synonym")
	public final List<String> synonyms = new ArrayList<String>();

	public String includedActivitiesStart;

	public String includedActivitiesEnd;

	public RichText allocationComment;

	public RichText generalComment;

	@XmlElement(name = "tag")
	public final List<String> tags = new ArrayList<String>();

	static Activity fromXml(Element e) {
		if (e == null)
			return null;
		Activity activity = new Activity();
		activity.id = e.getAttributeValue("id");
		activity.name = In.childText(e, "activityName");
		activity.generalComment = In.richText(e, "generalComment");
		activity.allocationComment = In.richText(e, "allocationComment");
		activity.includedActivitiesEnd = In.childText(e,
				"includedActivitiesEnd");
		activity.includedActivitiesStart = In.childText(e,
				"includedActivitiesStart");
		List<String> syns = In.childTexts(e, "synonym");
		activity.synonyms.addAll(syns);
		List<String> tags = In.childTexts(e, "tag");
		activity.tags.addAll(tags);

		activity.activityNameId = e.getAttributeValue("activityNameId");
		activity.activityNameContextId = e
				.getAttributeValue("activityNameContextId");
		activity.type = In.integer(e.getAttributeValue("type"));
		activity.specialActivityType = In.integer(e
				.getAttributeValue("specialActivityType"));
		activity.parentActivityId = e.getAttributeValue("parentActivityId");
		activity.parentActivityContextId = e
				.getAttributeValue("parentActivityContextId");
		activity.inheritanceDepth = In.optionalInteger(e
				.getAttributeValue("inheritanceDepth"));
		activity.energyValues = In.optionalInteger(e
				.getAttributeValue("energyValues"));
		activity.masterAllocationPropertyId = e
				.getAttributeValue("masterAllocationPropertyId");
		activity.masterAllocationPropertyContextId = e
				.getAttributeValue("masterAllocationPropertyContextId");
		return activity;
	}

	Element toXml() {
		Element e = new Element("activity", IO.NS);
		e.setAttribute("id", id);

		if (activityNameId != null)
			e.setAttribute("activityNameId", activityNameId);
		if (activityNameContextId != null)
			e.setAttribute("activityNameContextId", activityNameContextId);
		e.setAttribute("type", Integer.toString(type));
		e.setAttribute("specialActivityType",
				Integer.toString(specialActivityType));
		if (parentActivityId != null)
			e.setAttribute("parentActivityId", parentActivityId);
		if (parentActivityContextId != null)
			e.setAttribute("parentActivityContextId", parentActivityContextId);
		if (inheritanceDepth != null)
			e.setAttribute("inheritanceDepth", inheritanceDepth.toString());
		if (energyValues != null)
			e.setAttribute("energyValues", energyValues.toString());
		if (masterAllocationPropertyId != null)
			e.setAttribute("masterAllocationPropertyId",
					masterAllocationPropertyId);
		if (masterAllocationPropertyContextId != null)
			e.setAttribute("masterAllocationPropertyContextId",
					masterAllocationPropertyContextId);

		Out.addChild(e, "activityName", name);
		for (String synonym : synonyms)
			Out.addChild(e, "synonym", synonym);
		if (includedActivitiesStart != null)
			Out.addChild(e, "includedActivitiesStart", includedActivitiesStart);
		if (includedActivitiesEnd != null)
			Out.addChild(e, "includedActivitiesEnd", includedActivitiesEnd);
		if (allocationComment != null) {
			Element allocElement = Out.addChild(e, "allocationComment");
			Out.fill(allocElement, allocationComment);
		}
		if (generalComment != null) {
			Element commentElement = Out.addChild(e, "generalComment");
			Out.fill(commentElement, generalComment);
		}
		for (String tag : tags) {
			Out.addChild(e, "tag", tag);
		}
		return e;
	}
}
