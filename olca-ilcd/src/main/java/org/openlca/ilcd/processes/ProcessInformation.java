
package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Time;


/**
 * <p>Java class for ProcessInformationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProcessInformationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dataSetInformation" type="{http://lca.jrc.it/ILCD/Process}DataSetInformationType"/>
 *         &lt;element name="quantitativeReference" type="{http://lca.jrc.it/ILCD/Process}QuantitativeReferenceType" minOccurs="0"/>
 *         &lt;element name="time" type="{http://lca.jrc.it/ILCD/Common}TimeType" minOccurs="0"/>
 *         &lt;element name="geography" type="{http://lca.jrc.it/ILCD/Process}GeographyType" minOccurs="0"/>
 *         &lt;element name="technology" type="{http://lca.jrc.it/ILCD/Process}TechnologyType" minOccurs="0"/>
 *         &lt;element name="mathematicalRelations" type="{http://lca.jrc.it/ILCD/Process}MathematicalRelationsType" minOccurs="0"/>
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
@XmlType(name = "ProcessInformationType", propOrder = {
    "dataSetInformation",
    "quantitativeReference",
    "time",
    "geography",
    "technology",
    "parameters",
    "other"
})
public class ProcessInformation
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(required = true)
    protected DataSetInformation dataSetInformation;
    protected QuantitativeReference quantitativeReference;
    protected Time time;
    protected Geography geography;
    protected Technology technology;
    @XmlElement(name = "mathematicalRelations")
    protected ParameterList parameters;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected Other other;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes =  new HashMap<>();

    /**
     * Gets the value of the dataSetInformation property.
     * 
     * @return
     *     possible object is
     *     {@link DataSetInformation }
     *     
     */
    public DataSetInformation getDataSetInformation() {
        return dataSetInformation;
    }

    /**
     * Sets the value of the dataSetInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataSetInformation }
     *     
     */
    public void setDataSetInformation(DataSetInformation value) {
        this.dataSetInformation = value;
    }

    /**
     * Gets the value of the quantitativeReference property.
     * 
     * @return
     *     possible object is
     *     {@link QuantitativeReference }
     *     
     */
    public QuantitativeReference getQuantitativeReference() {
        return quantitativeReference;
    }

    /**
     * Sets the value of the quantitativeReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link QuantitativeReference }
     *     
     */
    public void setQuantitativeReference(QuantitativeReference value) {
        this.quantitativeReference = value;
    }

    /**
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link Time }
     *     
     */
    public Time getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link Time }
     *     
     */
    public void setTime(Time value) {
        this.time = value;
    }

    /**
     * Gets the value of the geography property.
     * 
     * @return
     *     possible object is
     *     {@link Geography }
     *     
     */
    public Geography getGeography() {
        return geography;
    }

    /**
     * Sets the value of the geography property.
     * 
     * @param value
     *     allowed object is
     *     {@link Geography }
     *     
     */
    public void setGeography(Geography value) {
        this.geography = value;
    }

    /**
     * Gets the value of the technology property.
     * 
     * @return
     *     possible object is
     *     {@link Technology }
     *     
     */
    public Technology getTechnology() {
        return technology;
    }

    /**
     * Sets the value of the technology property.
     * 
     * @param value
     *     allowed object is
     *     {@link Technology }
     *     
     */
    public void setTechnology(Technology value) {
        this.technology = value;
    }

    /**
     * Gets the value of the parameters property.
     * 
     * @return
     *     possible object is
     *     {@link ParameterList }
     *     
     */
    public ParameterList getParameters() {
        return parameters;
    }

    /**
     * Sets the value of the parameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParameterList }
     *     
     */
    public void setParameters(ParameterList value) {
        this.parameters = value;
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
