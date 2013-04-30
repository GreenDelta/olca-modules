package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Other;

/**
 * <p>
 * Java class for ProcessDataSetType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ProcessDataSetType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="processInformation" type="{http://lca.jrc.it/ILCD/Process}ProcessInformationType"/>
 *         &lt;element name="modellingAndValidation" type="{http://lca.jrc.it/ILCD/Process}ModellingAndValidationType" minOccurs="0"/>
 *         &lt;element name="administrativeInformation" type="{http://lca.jrc.it/ILCD/Process}AdministrativeInformationType" minOccurs="0"/>
 *         &lt;element name="exchanges" type="{http://lca.jrc.it/ILCD/Process}ExchangesType" minOccurs="0"/>
 *         &lt;element name="LCIAResults" type="{http://lca.jrc.it/ILCD/Process}LCIAResultsType" minOccurs="0"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}other" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" use="required" type="{http://lca.jrc.it/ILCD/Common}SchemaVersion" />
 *       &lt;attribute name="locations" type="{http://lca.jrc.it/ILCD/Common}String" />
 *       &lt;attribute name="metaDataOnly" type="{http://lca.jrc.it/ILCD/Common}boolean" default="false" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessDataSetType", propOrder = { "processInformation",
		"modellingAndValidation", "administrativeInformation", "exchanges",
		"lciaResults", "other" })
public class Process implements Serializable {

	private final static long serialVersionUID = 1L;
	@XmlElement(required = true)
	protected ProcessInformation processInformation;
	protected ModellingAndValidation modellingAndValidation;
	protected AdministrativeInformation administrativeInformation;
	protected ExchangeList exchanges;
	@XmlElement(name = "LCIAResults")
	protected LCIAResultList lciaResults;
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected Other other;
	@XmlAttribute(name = "version", required = true)
	protected String version;
	@XmlAttribute(name = "locations")
	protected String locations;
	@XmlAttribute(name = "metaDataOnly")
	protected Boolean metaDataOnly;
	@XmlAnyAttribute
	private Map<QName, String> otherAttributes = new HashMap<>();

	/**
	 * Gets the value of the processInformation property.
	 * 
	 * @return possible object is {@link ProcessInformation }
	 * 
	 */
	public ProcessInformation getProcessInformation() {
		return processInformation;
	}

	/**
	 * Sets the value of the processInformation property.
	 * 
	 * @param value
	 *            allowed object is {@link ProcessInformation }
	 * 
	 */
	public void setProcessInformation(ProcessInformation value) {
		this.processInformation = value;
	}

	/**
	 * Gets the value of the modellingAndValidation property.
	 * 
	 * @return possible object is {@link ModellingAndValidation }
	 * 
	 */
	public ModellingAndValidation getModellingAndValidation() {
		return modellingAndValidation;
	}

	/**
	 * Sets the value of the modellingAndValidation property.
	 * 
	 * @param value
	 *            allowed object is {@link ModellingAndValidation }
	 * 
	 */
	public void setModellingAndValidation(ModellingAndValidation value) {
		this.modellingAndValidation = value;
	}

	/**
	 * Gets the value of the administrativeInformation property.
	 * 
	 * @return possible object is {@link AdministrativeInformation }
	 * 
	 */
	public AdministrativeInformation getAdministrativeInformation() {
		return administrativeInformation;
	}

	/**
	 * Sets the value of the administrativeInformation property.
	 * 
	 * @param value
	 *            allowed object is {@link AdministrativeInformation }
	 * 
	 */
	public void setAdministrativeInformation(AdministrativeInformation value) {
		this.administrativeInformation = value;
	}

	/**
	 * Gets the value of the exchanges property.
	 * 
	 * @return possible object is {@link ExchangeList }
	 * 
	 */
	public ExchangeList getExchanges() {
		return exchanges;
	}

	/**
	 * Sets the value of the exchanges property.
	 * 
	 * @param value
	 *            allowed object is {@link ExchangeList }
	 * 
	 */
	public void setExchanges(ExchangeList value) {
		this.exchanges = value;
	}

	/**
	 * Gets the value of the lciaResults property.
	 * 
	 * @return possible object is {@link LCIAResultList }
	 * 
	 */
	public LCIAResultList getLCIAResults() {
		return lciaResults;
	}

	/**
	 * Sets the value of the lciaResults property.
	 * 
	 * @param value
	 *            allowed object is {@link LCIAResultList }
	 * 
	 */
	public void setLCIAResults(LCIAResultList value) {
		this.lciaResults = value;
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
	 * Gets the value of the version property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the value of the version property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setVersion(String value) {
		this.version = value;
	}

	/**
	 * Gets the value of the locations property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLocations() {
		return locations;
	}

	/**
	 * Sets the value of the locations property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLocations(String value) {
		this.locations = value;
	}

	/**
	 * Gets the value of the metaDataOnly property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isMetaDataOnly() {
		if (metaDataOnly == null)
			return false;
		return metaDataOnly;
	}

	/**
	 * Sets the value of the metaDataOnly property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setMetaDataOnly(Boolean value) {
		this.metaDataOnly = value;
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
