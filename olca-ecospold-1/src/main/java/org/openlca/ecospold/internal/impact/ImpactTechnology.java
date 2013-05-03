
package org.openlca.ecospold.internal.impact;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ecospold.ITechnology;


/**
 * Contains a description of the technology for which flow data have been collected. Free text can be used. Pictures, graphs and tables are not allowed. The text should cover information necessary to identify the properties and particularities of the technology(ies) underlying the process data.
 * 
 * <p>Java class for TTechnology complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TTechnology">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="text" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString32000" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TTechnology")
class ImpactTechnology
    implements Serializable, ITechnology
{

    private final static long serialVersionUID = 1L;
    @XmlAttribute(name = "text")
    protected String text;

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Override
	public String getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Override
	public void setText(String value) {
        this.text = value;
    }

}
