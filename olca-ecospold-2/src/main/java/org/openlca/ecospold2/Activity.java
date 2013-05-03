package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

public class Activity {

	private String id;
	private String name;
	private List<String> synonyms = new ArrayList<String>();
	private List<String> tags = new ArrayList<String>();
	private String includedActivitiesStart;
	private String includedActivitiesEnd;
	private String generalComment;

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

}
