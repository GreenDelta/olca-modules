package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}uuid" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}permanentUri" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}dataSetVersion" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}name" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}classification" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}generalComment" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}synonyms" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.ilcd-network.org/ILCD/ServiceAPI}TypeOfProcessValues" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}quantitativeReference" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}location" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}time" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}parameterized" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}hasResults" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}lciMethodInformation" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}completenessProductModel" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}complianceSystem" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}review" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}overallQuality" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}useAdvice" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}technicalPurpose" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}accessInformation" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}format" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}ownership" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}approvedBy" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{http://www.w3.org/1999/xlink}href"/>
 *       &lt;attribute ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}sourceId"/>
 *       &lt;attribute ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}accessRestricted"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "uuid", "permanentUri", "dataSetVersion",
		"name", "classification", "generalComment", "synonyms", "type",
		"quantitativeReference", "location", "time", "parameterized",
		"hasResults", "lciMethodInformation", "completenessProductModel",
		"complianceSystem", "review", "overallQuality", "useAdvice",
		"technicalPurpose", "accessInformation", "format", "ownership",
		"approvedBy" })
public class ProcessDescriptor implements Serializable {

	private final static long serialVersionUID = 1L;
	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	protected String uuid;
	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	@XmlSchemaType(name = "anyURI")
	protected String permanentUri;
	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	protected String dataSetVersion;
	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	protected LangString name;
	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	protected List<Classification> classification;
	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	protected LangString generalComment;
	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	protected List<LangString> synonyms;
	protected TypeOfProcessValues type;
	protected QuantitativeReferenceType quantitativeReference;
	protected String location;
	protected Time time;
	protected Boolean parameterized;
	protected Boolean hasResults;
	protected LciMethodInformation lciMethodInformation;
	protected CompletenessValues completenessProductModel;
	protected List<ComplianceSystem> complianceSystem;
	protected Review review;
	protected String overallQuality;
	protected LangString useAdvice;
	protected String technicalPurpose;
	protected AccessInfo accessInformation;
	protected String format;
	protected DataSetReference ownership;
	protected DataSetReference approvedBy;
	@XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
	@XmlSchemaType(name = "anyURI")
	protected String href;
	@XmlAttribute(name = "sourceId", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	protected String sourceId;
	@XmlAttribute(name = "accessRestricted", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	protected Boolean accessRestricted;

	/**
	 * Gets the value of the uuid property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * Sets the value of the uuid property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUuid(String value) {
		this.uuid = value;
	}

	/**
	 * Gets the value of the permanentUri property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPermanentUri() {
		return permanentUri;
	}

	/**
	 * Sets the value of the permanentUri property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPermanentUri(String value) {
		this.permanentUri = value;
	}

	/**
	 * Gets the value of the dataSetVersion property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDataSetVersion() {
		return dataSetVersion;
	}

	/**
	 * Sets the value of the dataSetVersion property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDataSetVersion(String value) {
		this.dataSetVersion = value;
	}

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link LangString }
	 * 
	 */
	public LangString getName() {
		return name;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link LangString }
	 * 
	 */
	public void setName(LangString value) {
		this.name = value;
	}

	/**
	 * Gets the value of the classification property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the classification property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getClassification().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Classification }
	 * 
	 * 
	 */
	public List<Classification> getClassification() {
		if (classification == null) {
			classification = new ArrayList<>();
		}
		return this.classification;
	}

	/**
	 * Gets the value of the generalComment property.
	 * 
	 * @return possible object is {@link LangString }
	 * 
	 */
	public LangString getGeneralComment() {
		return generalComment;
	}

	/**
	 * Sets the value of the generalComment property.
	 * 
	 * @param value
	 *            allowed object is {@link LangString }
	 * 
	 */
	public void setGeneralComment(LangString value) {
		this.generalComment = value;
	}

	/**
	 * Gets the value of the synonyms property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the synonyms property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getSynonyms().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link LangString }
	 * 
	 * 
	 */
	public List<LangString> getSynonyms() {
		if (synonyms == null) {
			synonyms = new ArrayList<>();
		}
		return this.synonyms;
	}

	/**
	 * Gets the value of the type property.
	 * 
	 * @return possible object is {@link TypeOfProcessValues }
	 * 
	 */
	public TypeOfProcessValues getType() {
		return type;
	}

	/**
	 * Sets the value of the type property.
	 * 
	 * @param value
	 *            allowed object is {@link TypeOfProcessValues }
	 * 
	 */
	public void setType(TypeOfProcessValues value) {
		this.type = value;
	}

	/**
	 * Gets the value of the quantitativeReference property.
	 * 
	 * @return possible object is {@link QuantitativeReferenceType }
	 * 
	 */
	public QuantitativeReferenceType getQuantitativeReference() {
		return quantitativeReference;
	}

	/**
	 * Sets the value of the quantitativeReference property.
	 * 
	 * @param value
	 *            allowed object is {@link QuantitativeReferenceType }
	 * 
	 */
	public void setQuantitativeReference(QuantitativeReferenceType value) {
		this.quantitativeReference = value;
	}

	/**
	 * Gets the value of the location property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Sets the value of the location property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLocation(String value) {
		this.location = value;
	}

	/**
	 * Gets the value of the time property.
	 * 
	 * @return possible object is {@link Time }
	 * 
	 */
	public Time getTime() {
		return time;
	}

	/**
	 * Sets the value of the time property.
	 * 
	 * @param value
	 *            allowed object is {@link Time }
	 * 
	 */
	public void setTime(Time value) {
		this.time = value;
	}

	/**
	 * Gets the value of the parameterized property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isParameterized() {
		return parameterized;
	}

	/**
	 * Sets the value of the parameterized property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setParameterized(Boolean value) {
		this.parameterized = value;
	}

	/**
	 * Gets the value of the hasResults property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isHasResults() {
		return hasResults;
	}

	/**
	 * Sets the value of the hasResults property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setHasResults(Boolean value) {
		this.hasResults = value;
	}

	/**
	 * Gets the value of the lciMethodInformation property.
	 * 
	 * @return possible object is {@link LciMethodInformation }
	 * 
	 */
	public LciMethodInformation getLciMethodInformation() {
		return lciMethodInformation;
	}

	/**
	 * Sets the value of the lciMethodInformation property.
	 * 
	 * @param value
	 *            allowed object is {@link LciMethodInformation }
	 * 
	 */
	public void setLciMethodInformation(LciMethodInformation value) {
		this.lciMethodInformation = value;
	}

	/**
	 * Gets the value of the completenessProductModel property.
	 * 
	 * @return possible object is {@link CompletenessValues }
	 * 
	 */
	public CompletenessValues getCompletenessProductModel() {
		return completenessProductModel;
	}

	/**
	 * Sets the value of the completenessProductModel property.
	 * 
	 * @param value
	 *            allowed object is {@link CompletenessValues }
	 * 
	 */
	public void setCompletenessProductModel(CompletenessValues value) {
		this.completenessProductModel = value;
	}

	/**
	 * Gets the value of the complianceSystem property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the complianceSystem property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getComplianceSystem().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ComplianceSystem }
	 * 
	 * 
	 */
	public List<ComplianceSystem> getComplianceSystem() {
		if (complianceSystem == null) {
			complianceSystem = new ArrayList<>();
		}
		return this.complianceSystem;
	}

	/**
	 * Gets the value of the review property.
	 * 
	 * @return possible object is {@link Review }
	 * 
	 */
	public Review getReview() {
		return review;
	}

	/**
	 * Sets the value of the review property.
	 * 
	 * @param value
	 *            allowed object is {@link Review }
	 * 
	 */
	public void setReview(Review value) {
		this.review = value;
	}

	/**
	 * Gets the value of the overallQuality property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getOverallQuality() {
		return overallQuality;
	}

	/**
	 * Sets the value of the overallQuality property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setOverallQuality(String value) {
		this.overallQuality = value;
	}

	/**
	 * Gets the value of the useAdvice property.
	 * 
	 * @return possible object is {@link LangString }
	 * 
	 */
	public LangString getUseAdvice() {
		return useAdvice;
	}

	/**
	 * Sets the value of the useAdvice property.
	 * 
	 * @param value
	 *            allowed object is {@link LangString }
	 * 
	 */
	public void setUseAdvice(LangString value) {
		this.useAdvice = value;
	}

	/**
	 * Gets the value of the technicalPurpose property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTechnicalPurpose() {
		return technicalPurpose;
	}

	/**
	 * Sets the value of the technicalPurpose property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTechnicalPurpose(String value) {
		this.technicalPurpose = value;
	}

	/**
	 * Gets the value of the accessInformation property.
	 * 
	 * @return possible object is {@link AccessInfo }
	 * 
	 */
	public AccessInfo getAccessInformation() {
		return accessInformation;
	}

	/**
	 * Sets the value of the accessInformation property.
	 * 
	 * @param value
	 *            allowed object is {@link AccessInfo }
	 * 
	 */
	public void setAccessInformation(AccessInfo value) {
		this.accessInformation = value;
	}

	/**
	 * Gets the value of the format property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * Sets the value of the format property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setFormat(String value) {
		this.format = value;
	}

	/**
	 * Gets the value of the ownership property.
	 * 
	 * @return possible object is {@link DataSetReference }
	 * 
	 */
	public DataSetReference getOwnership() {
		return ownership;
	}

	/**
	 * Sets the value of the ownership property.
	 * 
	 * @param value
	 *            allowed object is {@link DataSetReference }
	 * 
	 */
	public void setOwnership(DataSetReference value) {
		this.ownership = value;
	}

	/**
	 * Gets the value of the approvedBy property.
	 * 
	 * @return possible object is {@link DataSetReference }
	 * 
	 */
	public DataSetReference getApprovedBy() {
		return approvedBy;
	}

	/**
	 * Sets the value of the approvedBy property.
	 * 
	 * @param value
	 *            allowed object is {@link DataSetReference }
	 * 
	 */
	public void setApprovedBy(DataSetReference value) {
		this.approvedBy = value;
	}

	/**
	 * Gets the value of the href property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getHref() {
		return href;
	}

	/**
	 * Sets the value of the href property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setHref(String value) {
		this.href = value;
	}

	/**
	 * Gets the value of the sourceId property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSourceId() {
		return sourceId;
	}

	/**
	 * Sets the value of the sourceId property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSourceId(String value) {
		this.sourceId = value;
	}

	/**
	 * Gets the value of the accessRestricted property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isAccessRestricted() {
		return accessRestricted;
	}

	/**
	 * Sets the value of the accessRestricted property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setAccessRestricted(Boolean value) {
		this.accessRestricted = value;
	}

}
