package org.openlca.ilcd.sources;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.ClassificationInfo;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.FreeText;
import org.openlca.ilcd.commons.Label;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.PublicationType;

/**
 * <p>
 * Java class for DataSetInformationType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="DataSetInformationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}UUID"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}shortName" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="classificationInformation" type="{http://lca.jrc.it/ILCD/Common}ClassificationInformationType" minOccurs="0"/>
 *         &lt;element name="sourceCitation" type="{http://lca.jrc.it/ILCD/Common}ST" minOccurs="0"/>
 *         &lt;element name="publicationType" type="{http://lca.jrc.it/ILCD/Common}PublicationTypeValues" minOccurs="0"/>
 *         &lt;element name="sourceDescriptionOrComment" type="{http://lca.jrc.it/ILCD/Common}FTMultiLang" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="referenceToDigitalFile" type="{http://lca.jrc.it/ILCD/Source}ReferenceToDigitalFileType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="referenceToContact" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="referenceToLogo" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType" minOccurs="0"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}other" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataSetInformationType", propOrder = { "uuid", "shortName",
		"classificationInformation", "sourceCitation", "publicationType",
		"sourceDescriptionOrComment", "referenceToDigitalFile",
		"referenceToContact", "referenceToLogo", "other" })
public class DataSetInformation implements Serializable {

	private final static long serialVersionUID = 1L;
	@XmlElement(name = "UUID", namespace = "http://lca.jrc.it/ILCD/Common", required = true)
	protected String uuid;
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected List<Label> shortName;
	protected ClassificationInfo classificationInformation;
	protected String sourceCitation;
	protected PublicationType publicationType;
	protected List<FreeText> sourceDescriptionOrComment;
	protected List<DigitalFileReference> referenceToDigitalFile;
	protected List<DataSetReference> referenceToContact;
	protected DataSetReference referenceToLogo;
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected Other other;
	@XmlAnyAttribute
	private Map<QName, String> otherAttributes = new HashMap<>();

	/**
	 * Automatically generated Universally Unique Identifier of this data set.
	 * Together with the "Data set version", the UUID uniquely identifies each
	 * data set.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUUID() {
		return uuid;
	}

	/**
	 * Sets the value of the uuid property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUUID(String value) {
		this.uuid = value;
	}

	/**
	 * Short name for the "Source citation", i.e. for the bibliographical
	 * reference or reference to internal data sources used.Gets the value of
	 * the shortName property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the shortName property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getShortName().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Label }
	 * 
	 * 
	 */
	public List<Label> getShortName() {
		if (shortName == null) {
			shortName = new ArrayList<>();
		}
		return this.shortName;
	}

	/**
	 * Gets the value of the classificationInformation property.
	 * 
	 * @return possible object is {@link ClassificationInfo }
	 * 
	 */
	public ClassificationInfo getClassificationInformation() {
		return classificationInformation;
	}

	/**
	 * Sets the value of the classificationInformation property.
	 * 
	 * @param value
	 *            allowed object is {@link ClassificationInfo }
	 * 
	 */
	public void setClassificationInformation(ClassificationInfo value) {
		this.classificationInformation = value;
	}

	/**
	 * Gets the value of the sourceCitation property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSourceCitation() {
		return sourceCitation;
	}

	/**
	 * Sets the value of the sourceCitation property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSourceCitation(String value) {
		this.sourceCitation = value;
	}

	/**
	 * Gets the value of the publicationType property.
	 * 
	 * @return possible object is {@link PublicationType }
	 * 
	 */
	public PublicationType getPublicationType() {
		return publicationType;
	}

	/**
	 * Sets the value of the publicationType property.
	 * 
	 * @param value
	 *            allowed object is {@link PublicationType }
	 * 
	 */
	public void setPublicationType(PublicationType value) {
		this.publicationType = value;
	}

	/**
	 * Gets the value of the sourceDescriptionOrComment property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the sourceDescriptionOrComment property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getSourceDescriptionOrComment().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link FreeText }
	 * 
	 * 
	 */
	public List<FreeText> getSourceDescriptionOrComment() {
		if (sourceDescriptionOrComment == null) {
			sourceDescriptionOrComment = new ArrayList<>();
		}
		return this.sourceDescriptionOrComment;
	}

	/**
	 * Gets the value of the referenceToDigitalFile property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the referenceToDigitalFile property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getReferenceToDigitalFile().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link DigitalFileReference }
	 * 
	 * 
	 */
	public List<DigitalFileReference> getReferenceToDigitalFile() {
		if (referenceToDigitalFile == null) {
			referenceToDigitalFile = new ArrayList<>();
		}
		return this.referenceToDigitalFile;
	}

	/**
	 * Gets the value of the referenceToContact property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the referenceToContact property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getReferenceToContact().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link DataSetReference }
	 * 
	 * 
	 */
	public List<DataSetReference> getReferenceToContact() {
		if (referenceToContact == null) {
			referenceToContact = new ArrayList<>();
		}
		return this.referenceToContact;
	}

	/**
	 * Gets the value of the referenceToLogo property.
	 * 
	 * @return possible object is {@link DataSetReference }
	 * 
	 */
	public DataSetReference getReferenceToLogo() {
		return referenceToLogo;
	}

	/**
	 * Sets the value of the referenceToLogo property.
	 * 
	 * @param value
	 *            allowed object is {@link DataSetReference }
	 * 
	 */
	public void setReferenceToLogo(DataSetReference value) {
		this.referenceToLogo = value;
	}

	/**
	 * Gets the value of the other property.
	 * 
	 * @return possible object is {@link Other }
	 * 
	 */
	public Other getOther() {
		return other;
	}

	/**
	 * Sets the value of the other property.
	 * 
	 * @param value
	 *            allowed object is {@link Other }
	 * 
	 */
	public void setOther(Other value) {
		this.other = value;
	}

	/**
	 * Gets a map that contains attributes that aren't bound to any typed
	 * property on this class.
	 * 
	 * <p>
	 * the map is keyed by the name of the attribute and the value is the string
	 * value of the attribute.
	 * 
	 * the map returned by this method is live, and you can add new attribute by
	 * updating the map directly. Because of this design, there's no setter.
	 * 
	 * 
	 * @return always non-null
	 */
	public Map<QName, String> getOtherAttributes() {
		return otherAttributes;
	}

}
