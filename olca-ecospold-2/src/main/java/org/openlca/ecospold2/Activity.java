package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

public class Activity {

	public String id;
	public String activityNameId;
	public String activityNameContextId;
	public int type;
	public int specialActivityType;
	public String parentActivityId;
	public String parentActivityContextId;
	public Integer inheritanceDepth;
	public Integer energyValues;
	public String masterAllocationPropertyId;
	public String masterAllocationPropertyContextId;
	public Boolean masterAllocationPropertyIdOverwrittenByChild;
	public String name;
	public final List<String> synonyms = new ArrayList<String>();
	public final List<String> tags = new ArrayList<String>();
	public String includedActivitiesStart;
	public String includedActivitiesEnd;
	public String generalComment;
	public String allocationComment;

	static Activity fromXml(Element e) {
		if (e == null)
			return null;
		Activity activity = new Activity();
		activity.id = e.getAttributeValue("id");
		activity.name = In.childText(e, "activityName");
		List<Element> elems = In.childs(e, "generalComment", "text");
		activity.generalComment = In.joinText(elems);
		elems = In.childs(e, "allocationComment", "text");
		activity.allocationComment = In.joinText(elems);
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
		activity.masterAllocationPropertyIdOverwrittenByChild = In
				.optionalBool(e
						.getAttributeValue("masterAllocationPropertyIdOverwrittenByChild"));
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
		if (masterAllocationPropertyIdOverwrittenByChild != null)
			e.setAttribute("masterAllocationPropertyIdOverwrittenByChild",
					masterAllocationPropertyIdOverwrittenByChild.toString());

		Out.addChild(e, "activityName", name);
		for (String synonym : synonyms)
			Out.addChild(e, "synonym", synonym);
		if (includedActivitiesStart != null)
			Out.addChild(e, "includedActivitiesStart", includedActivitiesStart);
		if (includedActivitiesEnd != null)
			Out.addChild(e, "includedActivitiesEnd", includedActivitiesEnd);
		if (allocationComment != null) {
			Element allocElement = Out.addChild(e, "allocationComment");
			Out.addIndexedText(allocElement, allocationComment);
		}
		if (generalComment != null) {
			Element commentElement = Out.addChild(e, "generalComment");
			Out.addIndexedText(commentElement, generalComment);
		}
		for (String tag : tags) {
			Out.addChild(e, "tag", tag);
		}
		return e;
	}
}
