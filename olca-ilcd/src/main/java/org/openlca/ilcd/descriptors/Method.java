
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
 *       &lt;attribute name="name" use="required" type="{http://www.ilcd-network.org/ILCD/ServiceAPI}MethodOfReviewValues" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "method")
public class Method
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlAttribute(name = "name", required = true)
    protected MethodOfReviewValues name;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link MethodOfReviewValues }
     *     
     */
    public MethodOfReviewValues getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link MethodOfReviewValues }
     *     
     */
    public void setName(MethodOfReviewValues value) {
        this.name = value;
    }

}
