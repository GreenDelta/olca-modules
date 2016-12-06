package org.openlca.ecospold2;

import java.util.List;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class Source {

	public String id;
	public Integer sourceType;
	public Integer year;
	public String volumeNo;
	public String firstAuthor;
	public String additionalAuthors;
	public String title;
	public String titleOfAnthology;
	public String placeOfPublications;
	public String publisher;
	public String issueNo;
	public String journal;
	public String namesOfEditors;
	public String comment;

	static Source fromXml(Element e) {
		if (e == null)
			return null;
		Source source = new Source();
		source.id = e.getAttributeValue("id");
		source.sourceType = In.optionalInteger(e
				.getAttributeValue("sourceType"));
		source.year = In.optionalInteger(e.getAttributeValue("year"));
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

	Element toXml(Namespace ns) {
		Element element = new Element("source", ns);
		if (id != null)
			element.setAttribute("id", id);
		if (sourceType != null)
			element.setAttribute("sourceType", sourceType.toString());
		if (year != null)
			element.setAttribute("year", year.toString());
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
