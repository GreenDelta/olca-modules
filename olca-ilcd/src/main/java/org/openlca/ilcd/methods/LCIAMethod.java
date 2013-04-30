
package org.openlca.ilcd.methods;

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
 * <p>Java class for LCIAMethodDataSetType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LCIAMethodDataSetType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="LCIAMethodInformation" type="{http://lca.jrc.it/ILCD/LCIAMethod}LCIAMethodInformationType"/>
 *         &lt;element name="modellingAndValidation" type="{http://lca.jrc.it/ILCD/LCIAMethod}ModellingAndValidationType"/>
 *         &lt;element name="administrativeInformation" type="{http://lca.jrc.it/ILCD/LCIAMethod}AdministrativeInformationType" minOccurs="0"/>
 *         &lt;element name="characterisationFactors" type="{http://lca.jrc.it/ILCD/LCIAMethod}CharacterisationFactorsType" minOccurs="0"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}other" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" use="required" type="{http://lca.jrc.it/ILCD/Common}SchemaVersion" />
 *       &lt;attribute name="locations" type="{http://lca.jrc.it/ILCD/Common}String" />
 *       &lt;attribute name="LCIAMethodologies" type="{http://lca.jrc.it/ILCD/Common}String" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LCIAMethodDataSetType", propOrder = {
    "lciaMethodInformation",
    "modellingAndValidation",
    "administrativeInformation",
    "characterisationFactors",
    "other"
})
public class LCIAMethod
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "LCIAMethodInformation", required = true)
    protected LCIAMethodInformation lciaMethodInformation;
    @XmlElement(required = true)
    protected ModellingAndValidation modellingAndValidation;
    protected AdministrativeInformation administrativeInformation;
    protected FactorList characterisationFactors;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected Other other;
    @XmlAttribute(name = "version", required = true)
    protected String version;
    @XmlAttribute(name = "locations")
    protected String locations;
    @XmlAttribute(name = "LCIAMethodologies")
    protected String lciaMethodologies;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes =  new HashMap<>();

    /**
     * Gets the value of the lciaMethodInformation property.
     * 
     * @return
     *     possible object is
     *     {@link LCIAMethodInformation }
     *     
     */
    public LCIAMethodInformation getLCIAMethodInformation() {
        return lciaMethodInformation;
    }

    /**
     * Sets the value of the lciaMethodInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link LCIAMethodInformation }
     *     
     */
    public void setLCIAMethodInformation(LCIAMethodInformation value) {
        this.lciaMethodInformation = value;
    }

    /**
     * Gets the value of the modellingAndValidation property.
     * 
     * @return
     *     possible object is
     *     {@link ModellingAndValidation }
     *     
     */
    public ModellingAndValidation getModellingAndValidation() {
        return modellingAndValidation;
    }

    /**
     * Sets the value of the modellingAndValidation property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModellingAndValidation }
     *     
     */
    public void setModellingAndValidation(ModellingAndValidation value) {
        this.modellingAndValidation = value;
    }

    /**
     * Gets the value of the administrativeInformation property.
     * 
     * @return
     *     possible object is
     *     {@link AdministrativeInformation }
     *     
     */
    public AdministrativeInformation getAdministrativeInformation() {
        return administrativeInformation;
    }

    /**
     * Sets the value of the administrativeInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link AdministrativeInformation }
     *     
     */
    public void setAdministrativeInformation(AdministrativeInformation value) {
        this.administrativeInformation = value;
    }

    /**
     * Gets the value of the characterisationFactors property.
     * 
     * @return
     *     possible object is
     *     {@link FactorList }
     *     
     */
    public FactorList getCharacterisationFactors() {
        return characterisationFactors;
    }

    /**
     * Sets the value of the characterisationFactors property.
     * 
     * @param value
     *     allowed object is
     *     {@link FactorList }
     *     
     */
    public void setCharacterisationFactors(FactorList value) {
        this.characterisationFactors = value;
    }

    /**
     * Gets the value of the other property.
     * 
     * @return
     *     possible object is
     *     {@link Other }
     *     
     */
    public Other getOther() {
        return other;
    }

    /**
     * Sets the value of the other property.
     * 
     * @param value
     *     allowed object is
     *     {@link Other }
     *     
     */
    public void setOther(Other value) {
        this.other = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the locations property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocations() {
        return locations;
    }

    /**
     * Sets the value of the locations property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocations(String value) {
        this.locations = value;
    }

    /**
     * Gets the value of the lciaMethodologies property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLCIAMethodologies() {
        return lciaMethodologies;
    }

    /**
     * Sets the value of the lciaMethodologies property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLCIAMethodologies(String value) {
        this.lciaMethodologies = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
