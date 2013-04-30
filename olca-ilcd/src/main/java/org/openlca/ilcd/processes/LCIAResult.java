
package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.math.BigDecimal;
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
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.Label;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.UncertaintyDistribution;


/**
 * <p>Java class for LCIAResultType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LCIAResultType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="referenceToLCIAMethodDataSet" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType"/>
 *         &lt;element name="meanAmount" type="{http://lca.jrc.it/ILCD/Common}Real"/>
 *         &lt;element name="uncertaintyDistributionType" type="{http://lca.jrc.it/ILCD/Common}UncertaintyDistributionTypeValues" minOccurs="0"/>
 *         &lt;element name="relativeStandardDeviation95In" type="{http://lca.jrc.it/ILCD/Common}Perc" minOccurs="0"/>
 *         &lt;element name="generalComment" type="{http://lca.jrc.it/ILCD/Common}StringMultiLang" maxOccurs="100" minOccurs="0"/>
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
@XmlType(name = "LCIAResultType", propOrder = {
    "lciaMethod",
    "meanAmount",
    "uncertaintyDistribution",
    "relativeStandardDeviation95In",
    "generalComment",
    "other"
})
public class LCIAResult
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "referenceToLCIAMethodDataSet", required = true)
    protected DataSetReference lciaMethod;
    protected double meanAmount;
    @XmlElement(name = "uncertaintyDistributionType")
    protected UncertaintyDistribution uncertaintyDistribution;
    protected BigDecimal relativeStandardDeviation95In;
    protected List<Label> generalComment;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected Other other;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes =  new HashMap<>();

    /**
     * Gets the value of the lciaMethod property.
     * 
     * @return
     *     possible object is
     *     {@link DataSetReference }
     *     
     */
    public DataSetReference getLciaMethod() {
        return lciaMethod;
    }

    /**
     * Sets the value of the lciaMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataSetReference }
     *     
     */
    public void setLciaMethod(DataSetReference value) {
        this.lciaMethod = value;
    }

    /**
     * Gets the value of the meanAmount property.
     * 
     */
    public double getMeanAmount() {
        return meanAmount;
    }

    /**
     * Sets the value of the meanAmount property.
     * 
     */
    public void setMeanAmount(double value) {
        this.meanAmount = value;
    }

    /**
     * Gets the value of the uncertaintyDistribution property.
     * 
     * @return
     *     possible object is
     *     {@link UncertaintyDistribution }
     *     
     */
    public UncertaintyDistribution getUncertaintyDistribution() {
        return uncertaintyDistribution;
    }

    /**
     * Sets the value of the uncertaintyDistribution property.
     * 
     * @param value
     *     allowed object is
     *     {@link UncertaintyDistribution }
     *     
     */
    public void setUncertaintyDistribution(UncertaintyDistribution value) {
        this.uncertaintyDistribution = value;
    }

    /**
     * Gets the value of the relativeStandardDeviation95In property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getRelativeStandardDeviation95In() {
        return relativeStandardDeviation95In;
    }

    /**
     * Sets the value of the relativeStandardDeviation95In property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setRelativeStandardDeviation95In(BigDecimal value) {
        this.relativeStandardDeviation95In = value;
    }

    /**
     * Gets the value of the generalComment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the generalComment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGeneralComment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Label }
     * 
     * 
     */
    public List<Label> getGeneralComment() {
        if (generalComment == null) {
            generalComment = new ArrayList<>();
        }
        return this.generalComment;
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
