package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

public class Activity {

	private String id;
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
		return activity;
	}

	Element toXml() {
		Element e = new Element("activity", Out.NS);
		e.setAttribute("id", id);
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
