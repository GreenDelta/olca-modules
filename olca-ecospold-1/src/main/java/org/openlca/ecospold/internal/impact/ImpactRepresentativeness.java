
package org.openlca.ecospold.internal.impact;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ecospold.IRepresentativeness;


/**
 * Contains information about the fraction of the relevant market supplied by the product/service described in the dataset. Information about market share, production volume (in the ecoinvent quality network: also consumption volume in the market area) and information about how data have been sampled.
 * 
 * <p>Java class for TRepresentativeness complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TRepresentativeness">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="percent">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.EcoInvent.org/EcoSpold01Impact}TPercent">
 *             &lt;maxInclusive value="100.0"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="productionVolume" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString80" />
 *       &lt;attribute name="samplingProcedure" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString32000" />
 *       &lt;attribute name="extrapolations" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString32000" />
 *       &lt;attribute name="uncertaintyAdjustments" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString32000" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TRepresentativeness")
class ImpactRepresentativeness
    implements Serializable, IRepresentativeness
{

    private final static long serialVersionUID = 1L;
    @XmlAttribute(name = "percent")
    protected Float percent;
    @XmlAttribute(name = "productionVolume")
    protected String productionVolume;
    @XmlAttribute(name = "samplingProcedure")
    protected String samplingProcedure;
    @XmlAttribute(name = "extrapolations")
    protected String extrapolations;
    @XmlAttribute(name = "uncertaintyAdjustments")
    protected String uncertaintyAdjustments;

    /**
     * Gets the value of the percent property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    @Override
	public Float getPercent() {
        return percent;
    }

    /**
     * Sets the value of the percent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    @Override
	public void setPercent(Float value) {
        this.percent = value;
    }

    /**
     * Gets the value of the productionVolume property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Override
	public String getProductionVolume() {
        return productionVolume;
    }

    /**
     * Sets the value of the productionVolume property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Override
	public void setProductionVolume(String value) {
        this.productionVolume = value;
    }

    /**
     * Gets the value of the samplingProcedure property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Override
	public String getSamplingProcedure() {
        return samplingProcedure;
    }

    /**
     * Sets the value of the samplingProcedure property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Override
	public void setSamplingProcedure(String value) {
        this.samplingProcedure = value;
    }

    /**
     * Gets the value of the extrapolations property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Override
	public String getExtrapolations() {
        return extrapolations;
    }

    /**
     * Sets the value of the extrapolations property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Override
	public void setExtrapolations(String value) {
        this.extrapolations = value;
    }

    /**
     * Gets the value of the uncertaintyAdjustments property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Override
	public String getUncertaintyAdjustments() {
        return uncertaintyAdjustments;
    }

    /**
     * Sets the value of the uncertaintyAdjustments property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Override
	public void setUncertaintyAdjustments(String value) {
        this.uncertaintyAdjustments = value;
    }

}
