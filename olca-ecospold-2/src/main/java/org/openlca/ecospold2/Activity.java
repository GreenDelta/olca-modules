package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

public class Activity {

	private String id;
	private String activityNameId;
	private String activityNameContextId;
	private int type;
	private int specialActivityType;
	private String parentActivityId;
	private String parentActivityContextId;
	private Integer inheritanceDepth;
	private Integer energyValues;
	private String masterAllocationPropertyId;
	private String masterAllocationPropertyContextId;
	private Boolean masterAllocationPropertyIdOverwrittenByChild;
	private String name;
	private List<String> synonyms = new ArrayList<String>();
	private List<String> tags = new ArrayList<String>();
	private String includedActivitiesStart;
	private String includedActivitiesEnd;
	private String generalComment;
	private String allocationComment;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIncludedActivitiesStart() {
		return includedActivitiesStart;
	}

	public void setIncludedActivitiesStart(String includedActivitiesStart) {
		this.includedActivitiesStart = includedActivitiesStart;
	}

	public String getIncludedActivitiesEnd() {
		return includedActivitiesEnd;
	}

	public void setIncludedActivitiesEnd(String includedActivitiesEnd) {
		this.includedActivitiesEnd = includedActivitiesEnd;
	}

	public String getGeneralComment() {
		return generalComment;
	}

	public void setGeneralComment(String generalComment) {
		this.generalComment = generalComment;
	}

	public List<String> getSynonyms() {
		return synonyms;
	}

	public List<String> getTags() {
		return tags;
	}

	public String getAllocationComment() {
		return allocationComment;
	}

	public void setAllocationComment(String allocationComment) {
		this.allocationComment = allocationComment;
	}

	public String getActivityNameId() {
		return activityNameId;
	}

	public void setActivityNameId(String activityNameId) {
		this.activityNameId = activityNameId;
	}

	public String getActivityNameContextId() {
		return activityNameContextId;
	}

	public void setActivityNameContextId(String activityNameContextId) {
		this.activityNameContextId = activityNameContextId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getSpecialActivityType() {
		return specialActivityType;
	}

	public void setSpecialActivityType(int specialActivityType) {
		this.specialActivityType = specialActivityType;
	}

	public String getParentActivityId() {
		return parentActivityId;
	}

	public void setParentActivityId(String parentActivityId) {
		this.parentActivityId = parentActivityId;
	}

	public String getParentActivityContextId() {
		return parentActivityContextId;
	}

	public void setParentActivityContextId(String parentActivityContextId) {
		this.parentActivityContextId = parentActivityContextId;
	}

	public Integer getInheritanceDepth() {
		return inheritanceDepth;
	}

	public void setInheritanceDepth(Integer inheritanceDepth) {
		this.inheritanceDepth = inheritanceDepth;
	}

	public Integer getEnergyValues() {
		return energyValues;
	}

	public void setEnergyValues(Integer energyValues) {
		this.energyValues = energyValues;
	}

	public String getMasterAllocationPropertyId() {
		return masterAllocationPropertyId;
	}

	public void setMasterAllocationPropertyId(String masterAllocationPropertyId) {
		this.masterAllocationPropertyId = masterAllocationPropertyId;
	}

	public String getMasterAllocationPropertyContextId() {
		return masterAllocationPropertyContextId;
	}

	public void setMasterAllocationPropertyContextId(
			String masterAllocationPropertyContextId) {
		this.masterAllocationPropertyContextId = masterAllocationPropertyContextId;
	}

	public Boolean getMasterAllocationPropertyIdOverwrittenByChild() {
		return masterAllocationPropertyIdOverwrittenByChild;
	}

	public void setMasterAllocationPropertyIdOverwrittenByChild(
			Boolean masterAllocationPropertyIdOverwrittenByChild) {
		this.masterAllocationPropertyIdOverwrittenByChild = masterAllocationPropertyIdOverwrittenByChild;
	}

	static Activity fromXml(Element e) {
		if (e == null)
			return null;
		Activity activity = new Activity();
		activity.setId(e.getAttributeValue("id"));
		activity.setName(In.childText(e, "activityName"));
		List<Element> elems = In.childs(e, "generalComment", "text");
		activity.setGeneralComment(In.joinText(elems));
		elems = In.childs(e, "allocationComment", "text");
		activity.setAllocationComment(In.joinText(elems));
		activity.setIncludedActivitiesEnd(In.childText(e,
				"includedActivitiesEnd"));
		activity.setIncludedActivitiesStart(In.childText(e,
				"includedActivitiesStart"));
		List<String> syns = In.childTexts(e, "synonym");
		activity.getSynonyms().addAll(syns);
		List<String> tags = In.childTexts(e, "tag");
		activity.getTags().addAll(tags);

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
