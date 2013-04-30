
package org.openlca.ilcd.methods;

import java.io.Serializable;
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
import org.openlca.ilcd.commons.FreeText;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.ShortText;


/**
 * <p>Java class for ImpactModelType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ImpactModelType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modelName" type="{http://lca.jrc.it/ILCD/Common}ST" minOccurs="0"/>
 *         &lt;element name="modelDescription" type="{http://lca.jrc.it/ILCD/Common}FTMultiLang" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="referenceToModelSource" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="referenceToIncludedMethods" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="consideredMechanisms" type="{http://lca.jrc.it/ILCD/Common}STMultiLang" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="referenceToMethodologyFlowChart" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "ImpactModelType", propOrder = {
    "modelName",
    "modelDescription",
    "referenceToModelSource",
    "referenceToIncludedMethods",
    "consideredMechanisms",
    "referenceToMethodologyFlowChart",
    "other"
})
public class ImpactModel
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    protected String modelName;
    protected List<FreeText> modelDescription;
    protected List<DataSetReference> referenceToModelSource;
    protected List<DataSetReference> referenceToIncludedMethods;
    protected List<ShortText> consideredMechanisms;
    protected List<DataSetReference> referenceToMethodologyFlowChart;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected Other other;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes =  new HashMap<>();

    /**
     * Gets the value of the modelName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Sets the value of the modelName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModelName(String value) {
        this.modelName = value;
    }

    /**
     * Gets the value of the modelDescription property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the modelDescription property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getModelDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FreeText }
     * 
     * 
     */
    public List<FreeText> getModelDescription() {
        if (modelDescription == null) {
            modelDescription = new ArrayList<>();
        }
        return this.modelDescription;
    }

    /**
     * Gets the value of the referenceToModelSource property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the referenceToModelSource property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferenceToModelSource().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataSetReference }
     * 
     * 
     */
    public List<DataSetReference> getReferenceToModelSource() {
        if (referenceToModelSource == null) {
            referenceToModelSource = new ArrayList<>();
        }
        return this.referenceToModelSource;
    }

    /**
     * Gets the value of the referenceToIncludedMethods property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the referenceToIncludedMethods property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferenceToIncludedMethods().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataSetReference }
     * 
     * 
     */
    public List<DataSetReference> getReferenceToIncludedMethods() {
        if (referenceToIncludedMethods == null) {
            referenceToIncludedMethods = new ArrayList<>();
        }
        return this.referenceToIncludedMethods;
    }

    /**
     * Gets the value of the consideredMechanisms property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the consideredMechanisms property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConsideredMechanisms().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ShortText }
     * 
     * 
     */
    public List<ShortText> getConsideredMechanisms() {
        if (consideredMechanisms == null) {
            consideredMechanisms = new ArrayList<>();
        }
        return this.consideredMechanisms;
    }

    /**
     * Gets the value of the referenceToMethodologyFlowChart property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the referenceToMethodologyFlowChart property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferenceToMethodologyFlowChart().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataSetReference }
     * 
     * 
     */
    public List<DataSetReference> getReferenceToMethodologyFlowChart() {
        if (referenceToMethodologyFlowChart == null) {
            referenceToMethodologyFlowChart = new ArrayList<>();
        }
        return this.referenceToMethodologyFlowChart;
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
