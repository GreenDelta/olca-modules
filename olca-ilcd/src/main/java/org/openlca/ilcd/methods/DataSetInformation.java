package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.AreaOfProtection;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.annotations.FreeText;
import org.openlca.ilcd.commons.annotations.Label;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataSetInformationType", propOrder = { "uuid", "name",
		"methodology", "classifications", "impactCategory",
		"areaOfProtection", "impactIndicator", "generalComment",
		"referenceToExternalDocumentation", "other" })
public class DataSetInformation implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "UUID", namespace = "http://lca.jrc.it/ILCD/Common", required = true)
	protected String uuid;

	@Label
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected List<LangString> name;

	protected List<String> methodology;

	@XmlElementWrapper(name = "classificationInformation")
	@XmlElement(name = "classification", namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<Classification> classifications = new ArrayList<>();

	protected List<String> impactCategory;

	protected List<AreaOfProtection> areaOfProtection;

	protected String impactIndicator;

	@FreeText
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected List<LangString> generalComment;

	protected List<DataSetReference> referenceToExternalDocumentation;

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
	 * Name of the data set. Composed as follows "LCIA methodology short name;
	 * Impact category/ies; midpoint/endpoint; Impact indicator; Source short
	 * name" . Not applicable components are left out. Examples: "Impacts2007+;
	 * Climate change; midpoint; Global Warming Potential; IPCC 2001" ; "ABC
	 * 2006; Acidification; endpoint; Species diversity loss; John Doe 2006";
	 * "My-indicator2009; combined; endpoint; Ecopoints; various"Gets the value
	 * of the name property.
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
	public List<LangString> getName() {
		if (name == null) {
			name = new ArrayList<>();
		}
		return this.name;
	}

	/**
	 * Gets the value of the methodology property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the methodology property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getMethodology().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link String }
	 * 
	 * 
	 */
	public List<String> getMethodology() {
		if (methodology == null) {
			methodology = new ArrayList<>();
		}
		return this.methodology;
	}

	/**
	 * Gets the value of the impactCategory property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the impactCategory property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getImpactCategory().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link String }
	 * 
	 * 
	 */
	public List<String> getImpactCategory() {
		if (impactCategory == null) {
			impactCategory = new ArrayList<>();
		}
		return this.impactCategory;
	}

	/**
	 * Gets the value of the areaOfProtection property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the areaOfProtection property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getAreaOfProtection().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link AreaOfProtection }
	 * 
	 * 
	 */
	public List<AreaOfProtection> getAreaOfProtection() {
		if (areaOfProtection == null) {
			areaOfProtection = new ArrayList<>();
		}
		return this.areaOfProtection;
	}

	/**
	 * Gets the value of the impactIndicator property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getImpactIndicator() {
		return impactIndicator;
	}

	/**
	 * Sets the value of the impactIndicator property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setImpactIndicator(String value) {
		this.impactIndicator = value;
	}

	/**
	 * General information about the data set, including e.g. general (internal,
	 * not reviewed) quality statements as well as information sources used.
	 * (Note: Please also check the more specific fields e.g. on "Intended
	 * application", "Advice on data set use" and the fields in the "Modelling
	 * and validation" section to avoid overlapping entries.) Gets the value of
	 * the generalComment property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the generalComment property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getGeneralComment().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link FreeText
	 * }
	 * 
	 * 
	 */
	public List<LangString> getGeneralComment() {
		if (generalComment == null) {
			generalComment = new ArrayList<>();
		}
		return this.generalComment;
	}

	/**
	 * Gets the value of the referenceToExternalDocumentation property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the referenceToExternalDocumentation
	 * property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getReferenceToExternalDocumentation().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link DataSetReference }
	 * 
	 * 
	 */
	public List<DataSetReference> getReferenceToExternalDocumentation() {
		if (referenceToExternalDocumentation == null) {
			referenceToExternalDocumentation = new ArrayList<>();
		}
		return this.referenceToExternalDocumentation;
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
