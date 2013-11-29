package org.openlca.ecospold2;

import java.util.List;

import org.jdom2.Element;

public class Source {

	private String id;
	private Integer sourceType;
	private String year;
	private String volumeNo;
	private String firstAuthor;
	private String additionalAuthors;
	private String title;
	private String titleOfAnthology;
	private String placeOfPublications;
	private String publisher;
	private String issueNo;
	private String journal;
	private String namesOfEditors;
	private String comment;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getSourceType() {
		return sourceType;
	}

	public void setSourceType(Integer sourceType) {
		this.sourceType = sourceType;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getVolumeNo() {
		return volumeNo;
	}

	public void setVolumeNo(String volumeNo) {
		this.volumeNo = volumeNo;
	}

	public String getFirstAuthor() {
		return firstAuthor;
	}

	public void setFirstAuthor(String firstAuthor) {
		this.firstAuthor = firstAuthor;
	}

	public String getAdditionalAuthors() {
		return additionalAuthors;
	}

	public void setAdditionalAuthors(String additionalAuthors) {
		this.additionalAuthors = additionalAuthors;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitleOfAnthology() {
		return titleOfAnthology;
	}

	public void setTitleOfAnthology(String titleOfAnthology) {
		this.titleOfAnthology = titleOfAnthology;
	}

	public String getPlaceOfPublications() {
		return placeOfPublications;
	}

	public void setPlaceOfPublications(String placeOfPublications) {
		this.placeOfPublications = placeOfPublications;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getIssueNo() {
		return issueNo;
	}

	public void setIssueNo(String issueNo) {
		this.issueNo = issueNo;
	}

	public String getJournal() {
		return journal;
	}

	public void setJournal(String journal) {
		this.journal = journal;
	}

	public String getNamesOfEditors() {
		return namesOfEditors;
	}

	public void setNamesOfEditors(String namesOfEditors) {
		this.namesOfEditors = namesOfEditors;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	static Source fromXml(Element e) {
		if (e == null)
			return null;
		Source source = new Source();
		source.id = e.getAttributeValue("id");
		String sourceTypeStr = e.getAttributeValue("sourceType");
		if (sourceTypeStr != null)
			source.sourceType = In.integer(sourceTypeStr);
		source.year = e.getAttributeValue("year");
		source.volumeNo = e.getAttributeValue("volumeNo");
		source.firstAuthor = e.getAttributeValue("firstAuthor");
		source.additionalAuthors = e.getAttributeValue("additionalAuthors");
		source.title = e.getAttributeValue("title");
		source.titleOfAnthology = e.getAttributeValue("titleOfAnthology");
		source.placeOfPublications = e.getAttributeValue("placeOfPublications");
		source.publisher = e.getAttributeValue("publisher");
		source.issueNo = e.getAttributeValue("issueNo");
		source.journal = e.getAttributeValue("journal");
		source.namesOfEditors = e.getAttributeValue("namesOfEditors");
		List<Element> comments = In.childs(e, "comment");
		source.comment = In.joinText(comments);
		return source;
	}

	Element toXml() {
		Element element = new Element("source", Out.NS);
		if (id != null)
			element.setAttribute("id", id);
		if (sourceType != null)
			element.setAttribute("sourceType", sourceType.toString());
		if (year != null)
			element.setAttribute("year", year);
		if (volumeNo != null)
			element.setAttribute("volumeNo", volumeNo);
		if (firstAuthor != null)
			element.setAttribute("firstAuthor", firstAuthor);
		if (additionalAuthors != null)
			element.setAttribute("additionalAuthors", additionalAuthors);
		if (title != null)
			element.setAttribute("title", title);
		if (titleOfAnthology != null)
			element.setAttribute("titleOfAnthology", titleOfAnthology);
		if (placeOfPublications != null)
			element.setAttribute("placeOfPublications", placeOfPublications);
		if (publisher != null)
			element.setAttribute("publisher", publisher);
		if (issueNo != null)
			element.setAttribute("issueNo", issueNo);
		if (journal != null)
			element.setAttribute("journal", journal);
		if (namesOfEditors != null)
			element.setAttribute("namesOfEditors", namesOfEditors);
		if (comment != null)
			Out.addChild(element, "comment", comment);
		return element;
	}

}
