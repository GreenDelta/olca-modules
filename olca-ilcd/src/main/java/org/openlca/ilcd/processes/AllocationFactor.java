
package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AllocationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AllocationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="internalReferenceToCoProduct" type="{http://lca.jrc.it/ILCD/Common}Int6" />
 *       &lt;attribute name="allocatedFraction" type="{http://lca.jrc.it/ILCD/Common}Perc" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AllocationType")
public class AllocationFactor
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlAttribute(name = "internalReferenceToCoProduct")
    protected BigInteger referenceToCoProduct;
    @XmlAttribute(name = "allocatedFraction")
    protected BigDecimal allocatedFraction;

    /**
     * Gets the value of the referenceToCoProduct property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getReferenceToCoProduct() {
        return referenceToCoProduct;
    }

    /**
     * Sets the value of the referenceToCoProduct property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setReferenceToCoProduct(BigInteger value) {
        this.referenceToCoProduct = value;
    }

    /**
     * Gets the value of the allocatedFraction property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAllocatedFraction() {
        return allocatedFraction;
    }

    /**
     * Sets the value of the allocatedFraction property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAllocatedFraction(BigDecimal value) {
        this.allocatedFraction = value;
    }

}
