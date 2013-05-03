package org.openlca.ecospold.internal.process;

import java.io.Serializable;
import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ecospold.ISource;

/**
 * Contains information about author(s), title, kind of publication, place of
 * publication, name of editors (if any), etc..
 * 
 * <p>
 * Java class for TSource complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="TSource">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="number" use="required" type="{http://www.EcoInvent.org/EcoSpold01}TIndexNumber" />
 *       &lt;attribute name="sourceType" default="0">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *             &lt;minInclusive value="0"/>
 *             &lt;maxInclusive value="7"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="firstAuthor" use="required" type="{http://www.EcoInvent.org/EcoSpold01}TString40" />
 *       &lt;attribute name="additionalAuthors" type="{http://www.EcoInvent.org/EcoSpold01}TString255" />
 *       &lt;attribute name="year" use="required" type="{http://www.w3.org/2001/XMLSchema}gYear" />
 *       &lt;attribute name="title" use="required" type="{http://www.EcoInvent.org/EcoSpold01}TString32000" />
 *       &lt;attribute name="pageNumbers">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;maxLength value="15"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="nameOfEditors" type="{http://www.EcoInvent.org/EcoSpold01}TString40" />
 *       &lt;attribute name="titleOfAnthology" type="{http://www.EcoInvent.org/EcoSpold01}TString255" />
 *       &lt;attribute name="placeOfPublications" use="required" type="{http://www.EcoInvent.org/EcoSpold01}TString40" />
 *       &lt;attribute name="publisher" type="{http://www.EcoInvent.org/EcoSpold01}TString40" />
 *       &lt;attribute name="journal" type="{http://www.EcoInvent.org/EcoSpold01}TString40" />
 *       &lt;attribute name="volumeNo">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *             &lt;pattern value="\d{1,3}"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="issueNo" type="{http://www.EcoInvent.org/EcoSpold01}TString40" />
 *       &lt;attribute name="text" type="{http://www.EcoInvent.org/EcoSpold01}TString32000" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TSource")
class Source implements Serializable, ISource {

	private final static long serialVersionUID = 1L;
	@XmlAttribute(name = "number", required = true)
	protected int number;
	@XmlAttribute(name = "sourceType")
	protected Integer sourceType;
	@XmlAttribute(name = "firstAuthor", required = true)
	protected String firstAuthor;
	@XmlAttribute(name = "additionalAuthors")
	protected String additionalAuthors;
	@XmlAttribute(name = "year", required = true)
	@XmlSchemaType(name = "gYear")
	protected XMLGregorianCalendar year;
	@XmlAttribute(name = "title", required = true)
	protected String title;
	@XmlAttribute(name = "pageNumbers")
	protected String pageNumbers;
	@XmlAttribute(name = "nameOfEditors")
	protected String nameOfEditors;
	@XmlAttribute(name = "titleOfAnthology")
	protected String titleOfAnthology;
	@XmlAttribute(name = "placeOfPublications", required = true)
	protected String placeOfPublications;
	@XmlAttribute(name = "publisher")
	protected String publisher;
	@XmlAttribute(name = "journal")
	protected String journal;
	@XmlAttribute(name = "volumeNo")
	protected BigInteger volumeNo;
	@XmlAttribute(name = "issueNo")
	protected String issueNo;
	@XmlAttribute(name = "text")
	protected String text;

	/**
	 * Gets the value of the number property.
	 * 
	 */
	@Override
	public int getNumber() {
		return number;
	}

	/**
	 * Sets the value of the number property.
	 * 
	 */
	@Override
	public void setNumber(int value) {
		this.number = value;
	}

	/**
	 * Gets the value of the sourceType property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	@Override
	public int getSourceType() {
		if (sourceType == null)
			return 0;
		return sourceType;
	}

	/**
	 * Sets the value of the sourceType property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	@Override
	public void setSourceType(Integer value) {
		this.sourceType = value;
	}

	/**
	 * Gets the value of the firstAuthor property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getFirstAuthor() {
		return firstAuthor;
	}

	/**
	 * Sets the value of the firstAuthor property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setFirstAuthor(String value) {
		this.firstAuthor = value;
	}

	/**
	 * Gets the value of the additionalAuthors property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getAdditionalAuthors() {
		return additionalAuthors;
	}

	/**
	 * Sets the value of the additionalAuthors property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setAdditionalAuthors(String value) {
		this.additionalAuthors = value;
	}

	/**
	 * Gets the value of the year property.
	 * 
	 * @return possible object is {@link XMLGregorianCalendar }
	 * 
	 */
	@Override
	public XMLGregorianCalendar getYear() {
		return year;
	}

	/**
	 * Sets the value of the year property.
	 * 
	 * @param value
	 *            allowed object is {@link XMLGregorianCalendar }
	 * 
	 */
	@Override
	public void setYear(XMLGregorianCalendar value) {
		this.year = value;
	}

	/**
	 * Gets the value of the title property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the value of the title property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setTitle(String value) {
		this.title = value;
	}

	/**
	 * Gets the value of the pageNumbers property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getPageNumbers() {
		return pageNumbers;
	}

	/**
	 * Sets the value of the pageNumbers property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setPageNumbers(String value) {
		this.pageNumbers = value;
	}

	/**
	 * Gets the value of the nameOfEditors property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getNameOfEditors() {
		return nameOfEditors;
	}

	/**
	 * Sets the value of the nameOfEditors property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setNameOfEditors(String value) {
		this.nameOfEditors = value;
	}

	/**
	 * Gets the value of the titleOfAnthology property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getTitleOfAnthology() {
		return titleOfAnthology;
	}

	/**
	 * Sets the value of the titleOfAnthology property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setTitleOfAnthology(String value) {
		this.titleOfAnthology = value;
	}

	/**
	 * Gets the value of the placeOfPublications property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getPlaceOfPublications() {
		return placeOfPublications;
	}

	/**
	 * Sets the value of the placeOfPublications property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setPlaceOfPublications(String value) {
		this.placeOfPublications = value;
	}

	/**
	 * Gets the value of the publisher property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getPublisher() {
		return publisher;
	}

	/**
	 * Sets the value of the publisher property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setPublisher(String value) {
		this.publisher = value;
	}

	/**
	 * Gets the value of the journal property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getJournal() {
		return journal;
	}

	/**
	 * Sets the value of the journal property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setJournal(String value) {
		this.journal = value;
	}

	/**
	 * Gets the value of the volumeNo property.
	 * 
	 * @return possible object is {@link BigInteger }
	 * 
	 */
	@Override
	public BigInteger getVolumeNo() {
		return volumeNo;
	}

	/**
	 * Sets the value of the volumeNo property.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	@Override
	public void setVolumeNo(BigInteger value) {
		this.volumeNo = value;
	}

	/**
	 * Gets the value of the issueNo property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getIssueNo() {
		return issueNo;
	}

	/**
	 * Sets the value of the issueNo property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setIssueNo(String value) {
		this.issueNo = value;
	}

	/**
	 * Gets the value of the text property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getText() {
		return text;
	}

	/**
	 * Sets the value of the text property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setText(String value) {
		this.text = value;
	}

}
