package org.openlca.ilcd.contacts;

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

import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.Label;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.ShortText;

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
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}name" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="classificationInformation" type="{http://lca.jrc.it/ILCD/Common}ClassificationInformationType" minOccurs="0"/>
 *         &lt;element name="contactAddress" type="{http://lca.jrc.it/ILCD/Common}STMultiLang" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="telephone" type="{http://lca.jrc.it/ILCD/Common}String" minOccurs="0"/>
 *         &lt;element name="telefax" type="{http://lca.jrc.it/ILCD/Common}String" minOccurs="0"/>
 *         &lt;element name="email" type="{http://lca.jrc.it/ILCD/Common}String" minOccurs="0"/>
 *         &lt;element name="WWWAddress" type="{http://lca.jrc.it/ILCD/Common}ST" minOccurs="0"/>
 *         &lt;element name="centralContactPoint" type="{http://lca.jrc.it/ILCD/Common}STMultiLang" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="contactDescriptionOrComment" type="{http://lca.jrc.it/ILCD/Common}STMultiLang" maxOccurs="100" minOccurs="0"/>
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
		"name", "classificationInformation", "contactAddress", "telephone",
		"telefax", "email", "wwwAddress", "centralContactPoint", "description",
		"belongsTo", "logo", "other" })
public class DataSetInformation implements Serializable {

	private final static long serialVersionUID = 1L;
	@XmlElement(name = "UUID", namespace = "http://lca.jrc.it/ILCD/Common", required = true)
	protected String uuid;
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected List<Label> shortName;
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected List<Label> name;
	protected ClassificationInformation classificationInformation;
	protected List<ShortText> contactAddress;
	protected String telephone;
	protected String telefax;
	protected String email;
	@XmlElement(name = "WWWAddress")
	protected String wwwAddress;
	protected List<ShortText> centralContactPoint;
	@XmlElement(name = "contactDescriptionOrComment")
	protected List<ShortText> description;
	@XmlElement(name = "referenceToContact")
	protected List<DataSetReference> belongsTo;
	@XmlElement(name = "referenceToLogo")
	protected DataSetReference logo;
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
	 * Short name for the contact, that is used for display e.g. of links to
	 * this data set (especially in case the full name of the contact is rather
	 * long, e.g. "FAO" for "Food and Agriculture Organization").Gets the value
	 * of the shortName property.
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
	 * Name of the person, working group, organisation, or database network,
	 * which is represented by this contact data set.Gets the value of the name
	 * property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the name property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getName().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Label }
	 * 
	 * 
	 */
	public List<Label> getName() {
		if (name == null) {
			name = new ArrayList<>();
		}
		return this.name;
	}

	/**
	 * Gets the value of the classificationInformation property.
	 * 
	 * @return possible object is {@link ClassificationInformation }
	 * 
	 */
	public ClassificationInformation getClassificationInformation() {
		return classificationInformation;
	}

	/**
	 * Sets the value of the classificationInformation property.
	 * 
	 * @param value
	 *            allowed object is {@link ClassificationInformation }
	 * 
	 */
	public void setClassificationInformation(ClassificationInformation value) {
		this.classificationInformation = value;
	}

	/**
	 * Gets the value of the contactAddress property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the contactAddress property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getContactAddress().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ShortText }
	 * 
	 * 
	 */
	public List<ShortText> getContactAddress() {
		if (contactAddress == null) {
			contactAddress = new ArrayList<>();
		}
		return this.contactAddress;
	}

	/**
	 * Gets the value of the telephone property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTelephone() {
		return telephone;
	}

	/**
	 * Sets the value of the telephone property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTelephone(String value) {
		this.telephone = value;
	}

	/**
	 * Gets the value of the telefax property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTelefax() {
		return telefax;
	}

	/**
	 * Sets the value of the telefax property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTelefax(String value) {
		this.telefax = value;
	}

	/**
	 * Gets the value of the email property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the value of the email property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setEmail(String value) {
		this.email = value;
	}

	/**
	 * Gets the value of the wwwAddress property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getWWWAddress() {
		return wwwAddress;
	}

	/**
	 * Sets the value of the wwwAddress property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setWWWAddress(String value) {
		this.wwwAddress = value;
	}

	/**
	 * Gets the value of the centralContactPoint property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the centralContactPoint property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getCentralContactPoint().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ShortText }
	 * 
	 * 
	 */
	public List<ShortText> getCentralContactPoint() {
		if (centralContactPoint == null) {
			centralContactPoint = new ArrayList<>();
		}
		return this.centralContactPoint;
	}

	/**
	 * Gets the value of the description property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the description property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDescription().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ShortText }
	 * 
	 * 
	 */
	public List<ShortText> getDescription() {
		if (description == null) {
			description = new ArrayList<>();
		}
		return this.description;
	}

	/**
	 * Gets the value of the belongsTo property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the belongsTo property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getBelongsTo().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link DataSetReference }
	 * 
	 * 
	 */
	public List<DataSetReference> getBelongsTo() {
		if (belongsTo == null) {
			belongsTo = new ArrayList<>();
		}
		return this.belongsTo;
	}

	/**
	 * Gets the value of the logo property.
	 * 
	 * @return possible object is {@link DataSetReference }
	 * 
	 */
	public DataSetReference getLogo() {
		return logo;
	}

	/**
	 * Sets the value of the logo property.
	 * 
	 * @param value
	 *            allowed object is {@link DataSetReference }
	 * 
	 */
	public void setLogo(DataSetReference value) {
		this.logo = value;
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
