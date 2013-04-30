
package org.openlca.ilcd.processes;

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


/**
 * <p>Java class for TechnologyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TechnologyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="technologyDescriptionAndIncludedProcesses" type="{http://lca.jrc.it/ILCD/Common}FTMultiLang" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="referenceToIncludedProcesses" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="technologicalApplicability" type="{http://lca.jrc.it/ILCD/Common}FTMultiLang" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="referenceToTechnologyPictogramme" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType" minOccurs="0"/>
 *         &lt;element name="referenceToTechnologyFlowDiagrammOrPicture" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "TechnologyType", propOrder = {
    "technologyDescriptionAndIncludedProcesses",
    "referenceToIncludedProcesses",
    "technologicalApplicability",
    "referenceToTechnologyPictogramme",
    "referenceToTechnologyFlowDiagrammOrPicture",
    "other"
})
public class Technology
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    protected List<FreeText> technologyDescriptionAndIncludedProcesses;
    protected List<DataSetReference> referenceToIncludedProcesses;
    protected List<FreeText> technologicalApplicability;
    protected DataSetReference referenceToTechnologyPictogramme;
    protected List<DataSetReference> referenceToTechnologyFlowDiagrammOrPicture;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected Other other;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes =  new HashMap<>();

    /**
     * Gets the value of the technologyDescriptionAndIncludedProcesses property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the technologyDescriptionAndIncludedProcesses property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTechnologyDescriptionAndIncludedProcesses().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FreeText }
     * 
     * 
     */
    public List<FreeText> getTechnologyDescriptionAndIncludedProcesses() {
        if (technologyDescriptionAndIncludedProcesses == null) {
            technologyDescriptionAndIncludedProcesses = new ArrayList<>();
        }
        return this.technologyDescriptionAndIncludedProcesses;
    }

    /**
     * Gets the value of the referenceToIncludedProcesses property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the referenceToIncludedProcesses property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferenceToIncludedProcesses().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataSetReference }
     * 
     * 
     */
    public List<DataSetReference> getReferenceToIncludedProcesses() {
        if (referenceToIncludedProcesses == null) {
            referenceToIncludedProcesses = new ArrayList<>();
        }
        return this.referenceToIncludedProcesses;
    }

    /**
     * Gets the value of the technologicalApplicability property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the technologicalApplicability property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTechnologicalApplicability().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FreeText }
     * 
     * 
     */
    public List<FreeText> getTechnologicalApplicability() {
        if (technologicalApplicability == null) {
            technologicalApplicability = new ArrayList<>();
        }
        return this.technologicalApplicability;
    }

    /**
     * Gets the value of the referenceToTechnologyPictogramme property.
     * 
     * @return
     *     possible object is
     *     {@link DataSetReference }
     *     
     */
    public DataSetReference getReferenceToTechnologyPictogramme() {
        return referenceToTechnologyPictogramme;
    }

    /**
     * Sets the value of the referenceToTechnologyPictogramme property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataSetReference }
     *     
     */
    public void setReferenceToTechnologyPictogramme(DataSetReference value) {
        this.referenceToTechnologyPictogramme = value;
    }

    /**
     * Gets the value of the referenceToTechnologyFlowDiagrammOrPicture property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the referenceToTechnologyFlowDiagrammOrPicture property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferenceToTechnologyFlowDiagrammOrPicture().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataSetReference }
     * 
     * 
     */
    public List<DataSetReference> getReferenceToTechnologyFlowDiagrammOrPicture() {
        if (referenceToTechnologyFlowDiagrammOrPicture == null) {
            referenceToTechnologyFlowDiagrammOrPicture = new ArrayList<>();
        }
        return this.referenceToTechnologyFlowDiagrammOrPicture;
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
