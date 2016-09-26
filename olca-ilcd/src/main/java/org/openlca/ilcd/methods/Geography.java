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
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.FreeText;
import org.openlca.ilcd.commons.Location;
import org.openlca.ilcd.commons.Other;

/**
 * <p>
 * Java class for GeographyType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="GeographyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="interventionLocation" type="{http://lca.jrc.it/ILCD/Common}LocationType" minOccurs="0"/>
 *         &lt;element name="interventionSubLocation" type="{http://lca.jrc.it/ILCD/Common}LocationType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="impactLocation" type="{http://lca.jrc.it/ILCD/Common}LocationType" minOccurs="0"/>
 *         &lt;element name="geographicalRepresentativenessDescription" type="{http://lca.jrc.it/ILCD/Common}FTMultiLang" maxOccurs="100" minOccurs="0"/>
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
@XmlType(name = "GeographyType", propOrder = { "interventionLocation",
		"interventionSubLocation", "impactLocation",
		"geographicalRepresentativenessDescription", "other" })
public class Geography implements Serializable {

	private final static long serialVersionUID = 1L;
	protected Location interventionLocation;
	protected List<Location> interventionSubLocation;
	protected Location impactLocation;
	protected List<FreeText> geographicalRepresentativenessDescription;
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected Other other;
	@XmlAnyAttribute
	private Map<QName, String> otherAttributes = new HashMap<>();

	/**
	 * Gets the value of the interventionLocation property.
	 * 
	 * @return possible object is {@link Location }
	 * 
	 */
	public Location getInterventionLocation() {
		return interventionLocation;
	}

	/**
	 * Sets the value of the interventionLocation property.
	 * 
	 * @param value
	 *            allowed object is {@link Location }
	 * 
	 */
	public void setInterventionLocation(Location value) {
		this.interventionLocation = value;
	}

	/**
	 * Gets the value of the interventionSubLocation property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the interventionSubLocation property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getInterventionSubLocation().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Location }
	 * 
	 * 
	 */
	public List<Location> getInterventionSubLocation() {
		if (interventionSubLocation == null) {
			interventionSubLocation = new ArrayList<>();
		}
		return this.interventionSubLocation;
	}

	/**
	 * Gets the value of the impactLocation property.
	 * 
	 * @return possible object is {@link Location }
	 * 
	 */
	public Location getImpactLocation() {
		return impactLocation;
	}

	/**
	 * Sets the value of the impactLocation property.
	 * 
	 * @param value
	 *            allowed object is {@link Location }
	 * 
	 */
	public void setImpactLocation(Location value) {
		this.impactLocation = value;
	}

	/**
	 * Gets the value of the geographicalRepresentativenessDescription property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the geographicalRepresentativenessDescription
	 * property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getGeographicalRepresentativenessDescription().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link FreeText }
	 * 
	 * 
	 */
	public List<FreeText> getGeographicalRepresentativenessDescription() {
		if (geographicalRepresentativenessDescription == null) {
			geographicalRepresentativenessDescription = new ArrayList<>();
		}
		return this.geographicalRepresentativenessDescription;
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
