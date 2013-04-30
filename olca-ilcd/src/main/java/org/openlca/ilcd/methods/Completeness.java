
package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CompletenessType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CompletenessType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="completenessImpactCoverage" type="{http://lca.jrc.it/ILCD/Common}Perc" minOccurs="0"/>
 *         &lt;element name="inventoryItems" type="{http://lca.jrc.it/ILCD/Common}Int6" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompletenessType", propOrder = {
    "completenessImpactCoverage",
    "inventoryItems"
})
public class Completeness
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    protected BigDecimal completenessImpactCoverage;
    protected BigInteger inventoryItems;

    /**
     * Gets the value of the completenessImpactCoverage property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCompletenessImpactCoverage() {
        return completenessImpactCoverage;
    }

    /**
     * Sets the value of the completenessImpactCoverage property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCompletenessImpactCoverage(BigDecimal value) {
        this.completenessImpactCoverage = value;
    }

    /**
     * Gets the value of the inventoryItems property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getInventoryItems() {
        return inventoryItems;
    }

    /**
     * Sets the value of the inventoryItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setInventoryItems(BigInteger value) {
        this.inventoryItems = value;
    }

}
