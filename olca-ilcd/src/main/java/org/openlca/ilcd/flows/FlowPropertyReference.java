
package org.openlca.ilcd.flows;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.FlowDataDerivation;
import org.openlca.ilcd.commons.Label;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.UncertaintyDistribution;


/**
 * <p>Java class for FlowPropertyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FlowPropertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="referenceToFlowPropertyDataSet" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType"/>
 *         &lt;element name="meanValue" type="{http://lca.jrc.it/ILCD/Common}Real"/>
 *         &lt;element name="minimumValue" type="{http://lca.jrc.it/ILCD/Common}Real" minOccurs="0"/>
 *         &lt;element name="maximumValue" type="{http://lca.jrc.it/ILCD/Common}Real" minOccurs="0"/>
 *         &lt;element name="uncertaintyDistributionType" type="{http://lca.jrc.it/ILCD/Common}UncertaintyDistributionTypeValues" minOccurs="0"/>
 *         &lt;element name="relativeStandardDeviation95In" type="{http://lca.jrc.it/ILCD/Common}Perc" minOccurs="0"/>
 *         &lt;element name="dataDerivationTypeStatus" type="{http://lca.jrc.it/ILCD/Common}FlowDataDerivationTypeStatusValues" minOccurs="0"/>
 *         &lt;element name="generalComment" type="{http://lca.jrc.it/ILCD/Common}StringMultiLang" maxOccurs="100" minOccurs="0"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}other" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="dataSetInternalID" type="{http://lca.jrc.it/ILCD/Common}Int5" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FlowPropertyType", propOrder = {
    "flowProperty",
    "meanValue",
    "minimumValue",
    "maximumValue",
    "uncertaintyDistribution",
    "relativeStandardDeviation95In",
    "dataDerivation",
    "generalComment",
    "other"
})
public class FlowPropertyReference
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "referenceToFlowPropertyDataSet", required = true)
    protected DataSetReference flowProperty;
    protected double meanValue;
    protected Double minimumValue;
    protected Double maximumValue;
    @XmlElement(name = "uncertaintyDistributionType")
    protected UncertaintyDistribution uncertaintyDistribution;
    protected BigDecimal relativeStandardDeviation95In;
    @XmlElement(name = "dataDerivationTypeStatus")
    protected FlowDataDerivation dataDerivation;
    protected List<Label> generalComment;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected Other other;
    @XmlAttribute(name = "dataSetInternalID")
    protected BigInteger dataSetInternalID;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes =  new HashMap<>();

    /**
     * Gets the value of the flowProperty property.
     * 
     * @return
     *     possible object is
     *     {@link DataSetReference }
     *     
     */
    public DataSetReference getFlowProperty() {
        return flowProperty;
    }

    /**
     * Sets the value of the flowProperty property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataSetReference }
     *     
     */
    public void setFlowProperty(DataSetReference value) {
        this.flowProperty = value;
    }

    /**
     * Gets the value of the meanValue property.
     * 
     */
    public double getMeanValue() {
        return meanValue;
    }

    /**
     * Sets the value of the meanValue property.
     * 
     */
    public void setMeanValue(double value) {
        this.meanValue = value;
    }

    /**
     * Gets the value of the minimumValue property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMinimumValue() {
        return minimumValue;
    }

    /**
     * Sets the value of the minimumValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMinimumValue(Double value) {
        this.minimumValue = value;
    }

    /**
     * Gets the value of the maximumValue property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMaximumValue() {
        return maximumValue;
    }

    /**
     * Sets the value of the maximumValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMaximumValue(Double value) {
        this.maximumValue = value;
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
     * Gets the value of the dataDerivation property.
     * 
     * @return
     *     possible object is
     *     {@link FlowDataDerivation }
     *     
     */
    public FlowDataDerivation getDataDerivation() {
        return dataDerivation;
    }

    /**
     * Sets the value of the dataDerivation property.
     * 
     * @param value
     *     allowed object is
     *     {@link FlowDataDerivation }
     *     
     */
    public void setDataDerivation(FlowDataDerivation value) {
        this.dataDerivation = value;
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
     * Gets the value of the dataSetInternalID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDataSetInternalID() {
        return dataSetInternalID;
    }

    /**
     * Sets the value of the dataSetInternalID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDataSetInternalID(BigInteger value) {
        this.dataSetInternalID = value;
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
