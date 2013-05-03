
package org.openlca.ecospold.internal.impact;

import java.io.Serializable;
import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ecospold.IDataEntryBy;


/**
 * Contains information about the person that entered data in the database or transformed data into the format of the ecoinvent (or any other) quality network.
 * 
 * <p>Java class for TDataEntryBy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TDataEntryBy">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="person" use="required" type="{http://www.EcoInvent.org/EcoSpold01Impact}TIndexNumber" />
 *       &lt;attribute name="qualityNetwork" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TDataEntryBy")
class ImpactDataEntryBy
    implements Serializable, IDataEntryBy
{

    private final static long serialVersionUID = 1L;
    @XmlAttribute(name = "person", required = true)
    protected int person;
    @XmlAttribute(name = "qualityNetwork")
    protected BigInteger qualityNetwork;

    /**
     * Gets the value of the person property.
     * 
     */
    @Override
	public int getPerson() {
        return person;
    }

    /**
     * Sets the value of the person property.
     * 
     */
    @Override
	public void setPerson(int value) {
        this.person = value;
    }

    /**
     * Gets the value of the qualityNetwork property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    @Override
	public BigInteger getQualityNetwork() {
        return qualityNetwork;
    }

    /**
     * Sets the value of the qualityNetwork property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    @Override
	public void setQualityNetwork(BigInteger value) {
        this.qualityNetwork = value;
    }

}
