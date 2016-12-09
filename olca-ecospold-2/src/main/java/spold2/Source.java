package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Source {

	@XmlAttribute
	public String id;

	@XmlAttribute
	public Integer sourceType;

	@XmlAttribute
	public Integer year;

	@XmlAttribute
	public String volumeNo;

	@XmlAttribute
	public String firstAuthor;

	@XmlAttribute
	public String additionalAuthors;

	@XmlAttribute
	public String title;

	@XmlAttribute
	public String titleOfAnthology;

	@XmlAttribute
	public String placeOfPublications;

	@XmlAttribute
	public String publisher;

	@XmlAttribute
	public String issueNo;

	@XmlAttribute
	public String journal;

	@XmlAttribute
	public String namesOfEditors;

	@XmlElement
	public String comment;

}
