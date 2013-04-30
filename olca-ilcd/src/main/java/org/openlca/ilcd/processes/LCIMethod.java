package org.openlca.ilcd.processes;

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

import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.FreeText;
import org.openlca.ilcd.commons.LCIMethodApproach;
import org.openlca.ilcd.commons.LCIMethodPrinciple;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.ProcessType;

/**
 * <p>
 * Java class for LCIMethodAndAllocationType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="LCIMethodAndAllocationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="typeOfDataSet" type="{http://lca.jrc.it/ILCD/Common}TypeOfProcessValues" minOccurs="0"/>
 *         &lt;element name="LCIMethodPrinciple" type="{http://lca.jrc.it/ILCD/Common}LCIMethodPrincipleValues" minOccurs="0"/>
 *         &lt;element name="deviationsFromLCIMethodPrinciple" type="{http://lca.jrc.it/ILCD/Common}FTMultiLang" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="LCIMethodApproaches" type="{http://lca.jrc.it/ILCD/Common}LCIMethodApproachesValues" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="deviationsFromLCIMethodApproaches" type="{http://lca.jrc.it/ILCD/Common}FTMultiLang" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="modellingConstants" type="{http://lca.jrc.it/ILCD/Common}FTMultiLang" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="deviationsFromModellingConstants" type="{http://lca.jrc.it/ILCD/Common}FTMultiLang" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="referenceToLCAMethodDetails" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "LCIMethodAndAllocationType", propOrder = { "processType",
		"lciMethodPrinciple", "deviationsFromLCIMethodPrinciple",
		"lciMethodApproaches", "deviationsFromLCIMethodApproaches",
		"modellingConstants", "deviationsFromModellingConstants",
		"referenceToLCAMethodDetails", "other" })
public class LCIMethod implements Serializable {

	private final static long serialVersionUID = 1L;
	@XmlElement(name = "typeOfDataSet")
	protected ProcessType processType;
	@XmlElement(name = "LCIMethodPrinciple")
	protected LCIMethodPrinciple lciMethodPrinciple;
	protected List<FreeText> deviationsFromLCIMethodPrinciple;
	@XmlElement(name = "LCIMethodApproaches")
	protected List<LCIMethodApproach> lciMethodApproaches;
	protected List<FreeText> deviationsFromLCIMethodApproaches;
	protected List<FreeText> modellingConstants;
	protected List<FreeText> deviationsFromModellingConstants;
	protected List<DataSetReference> referenceToLCAMethodDetails;
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected Other other;
	@XmlAnyAttribute
	private Map<QName, String> otherAttributes = new HashMap<>();

	/**
	 * Gets the value of the processType property.
	 * 
	 * @return possible object is {@link ProcessType }
	 * 
	 */
	public ProcessType getProcessType() {
		return processType;
	}

	/**
	 * Sets the value of the processType property.
	 * 
	 * @param value
	 *            allowed object is {@link ProcessType }
	 * 
	 */
	public void setProcessType(ProcessType value) {
		this.processType = value;
	}

	/**
	 * Gets the value of the lciMethodPrinciple property.
	 * 
	 * @return possible object is {@link LCIMethodPrinciple }
	 * 
	 */
	public LCIMethodPrinciple getLCIMethodPrinciple() {
		return lciMethodPrinciple;
	}

	/**
	 * Sets the value of the lciMethodPrinciple property.
	 * 
	 * @param value
	 *            allowed object is {@link LCIMethodPrinciple }
	 * 
	 */
	public void setLCIMethodPrinciple(LCIMethodPrinciple value) {
		this.lciMethodPrinciple = value;
	}

	/**
	 * Gets the value of the deviationsFromLCIMethodPrinciple property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the deviationsFromLCIMethodPrinciple
	 * property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDeviationsFromLCIMethodPrinciple().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link FreeText }
	 * 
	 * 
	 */
	public List<FreeText> getDeviationsFromLCIMethodPrinciple() {
		if (deviationsFromLCIMethodPrinciple == null) {
			deviationsFromLCIMethodPrinciple = new ArrayList<>();
		}
		return this.deviationsFromLCIMethodPrinciple;
	}

	/**
	 * Gets the value of the lciMethodApproaches property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the lciMethodApproaches property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getLCIMethodApproaches().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link LCIMethodApproach }
	 * 
	 * 
	 */
	public List<LCIMethodApproach> getLCIMethodApproaches() {
		if (lciMethodApproaches == null) {
			lciMethodApproaches = new ArrayList<>();
		}
		return this.lciMethodApproaches;
	}

	/**
	 * Gets the value of the deviationsFromLCIMethodApproaches property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the deviationsFromLCIMethodApproaches
	 * property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDeviationsFromLCIMethodApproaches().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link FreeText }
	 * 
	 * 
	 */
	public List<FreeText> getDeviationsFromLCIMethodApproaches() {
		if (deviationsFromLCIMethodApproaches == null) {
			deviationsFromLCIMethodApproaches = new ArrayList<>();
		}
		return this.deviationsFromLCIMethodApproaches;
	}

	/**
	 * Gets the value of the modellingConstants property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the modellingConstants property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getModellingConstants().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link FreeText }
	 * 
	 * 
	 */
	public List<FreeText> getModellingConstants() {
		if (modellingConstants == null) {
			modellingConstants = new ArrayList<>();
		}
		return this.modellingConstants;
	}

	/**
	 * Gets the value of the deviationsFromModellingConstants property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the deviationsFromModellingConstants
	 * property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDeviationsFromModellingConstants().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link FreeText }
	 * 
	 * 
	 */
	public List<FreeText> getDeviationsFromModellingConstants() {
		if (deviationsFromModellingConstants == null) {
			deviationsFromModellingConstants = new ArrayList<>();
		}
		return this.deviationsFromModellingConstants;
	}

	/**
	 * Gets the value of the referenceToLCAMethodDetails property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the referenceToLCAMethodDetails property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getReferenceToLCAMethodDetails().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link DataSetReference }
	 * 
	 * 
	 */
	public List<DataSetReference> getReferenceToLCAMethodDetails() {
		if (referenceToLCAMethodDetails == null) {
			referenceToLCAMethodDetails = new ArrayList<>();
		}
		return this.referenceToLCAMethodDetails;
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
