
package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" use="required" type="{http://www.ilcd-network.org/ILCD/ServiceAPI}DataQualityIndicatorValues" />
 *       &lt;attribute name="value" use="required" type="{http://www.ilcd-network.org/ILCD/ServiceAPI}QualityValues" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "dataQualityIndicator")
public class DataQualityIndicator
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlAttribute(name = "name", required = true)
    protected DataQualityIndicatorValues name;
    @XmlAttribute(name = "value", required = true)
    protected QualityValues value;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link DataQualityIndicatorValues }
     *     
     */
    public DataQualityIndicatorValues getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataQualityIndicatorValues }
     *     
     */
    public void setName(DataQualityIndicatorValues value) {
        this.name = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link QualityValues }
     *     
     */
    public QualityValues getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link QualityValues }
     *     
     */
    public void setValue(QualityValues value) {
        this.value = value;
    }

}
