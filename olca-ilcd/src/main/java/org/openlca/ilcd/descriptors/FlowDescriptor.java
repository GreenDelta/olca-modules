
package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
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
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}uuid" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}permanentUri" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}dataSetVersion" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}name" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}classification" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}generalComment" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}synonyms" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Flow}flowCategorization" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.ilcd-network.org/ILCD/ServiceAPI}TypeOfFlowValues" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Flow}casNumber" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Flow}sumFormula" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Flow}referenceFlowProperty" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{http://www.w3.org/1999/xlink}href"/>
 *       &lt;attribute ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}sourceId"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "uuid",
    "permanentUri",
    "dataSetVersion",
    "name",
    "classification",
    "generalComment",
    "synonyms",
    "flowCategorization",
    "type",
    "casNumber",
    "sumFormula",
    "referenceFlowProperty"
})
public class FlowDescriptor implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
    protected String uuid;
    @XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
    @XmlSchemaType(name = "anyURI")
    protected String permanentUri;
    @XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
    protected String dataSetVersion;
    @XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
    protected LangString name;
    @XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
    protected List<Classification> classification;
    @XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
    protected LangString generalComment;
    @XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
    protected List<LangString> synonyms;
    @XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Flow")
    protected List<FlowCategorization> flowCategorization;
    @XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Flow")
    protected TypeOfFlowValues type;
    @XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Flow")
    protected String casNumber;
    @XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Flow")
    protected String sumFormula;
    @XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Flow")
    protected ReferenceFlowProperty referenceFlowProperty;
    @XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
    @XmlSchemaType(name = "anyURI")
    protected String href;
    @XmlAttribute(name = "sourceId", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
    protected String sourceId;

    /**
     * Gets the value of the uuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the value of the uuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUuid(String value) {
        this.uuid = value;
    }

    /**
     * Gets the value of the permanentUri property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPermanentUri() {
        return permanentUri;
    }

    /**
     * Sets the value of the permanentUri property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPermanentUri(String value) {
        this.permanentUri = value;
    }

    /**
     * Gets the value of the dataSetVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataSetVersion() {
        return dataSetVersion;
    }

    /**
     * Sets the value of the dataSetVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataSetVersion(String value) {
        this.dataSetVersion = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link LangString }
     *     
     */
    public LangString getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link LangString }
     *     
     */
    public void setName(LangString value) {
        this.name = value;
    }

    /**
     * Gets the value of the classification property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the classification property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClassification().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Classification }
     * 
     * 
     */
    public List<Classification> getClassification() {
        if (classification == null) {
            classification = new ArrayList<>();
        }
        return this.classification;
    }

    /**
     * Gets the value of the generalComment property.
     * 
     * @return
     *     possible object is
     *     {@link LangString }
     *     
     */
    public LangString getGeneralComment() {
        return generalComment;
    }

    /**
     * Sets the value of the generalComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link LangString }
     *     
     */
    public void setGeneralComment(LangString value) {
        this.generalComment = value;
    }

    /**
     * Gets the value of the synonyms property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the synonyms property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSynonyms().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LangString }
     * 
     * 
     */
    public List<LangString> getSynonyms() {
        if (synonyms == null) {
            synonyms = new ArrayList<>();
        }
        return this.synonyms;
    }

    /**
     * Gets the value of the flowCategorization property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the flowCategorization property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFlowCategorization().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FlowCategorization }
     * 
     * 
     */
    public List<FlowCategorization> getFlowCategorization() {
        if (flowCategorization == null) {
            flowCategorization = new ArrayList<>();
        }
        return this.flowCategorization;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link TypeOfFlowValues }
     *     
     */
    public TypeOfFlowValues getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeOfFlowValues }
     *     
     */
    public void setType(TypeOfFlowValues value) {
        this.type = value;
    }

    /**
     * Gets the value of the casNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCasNumber() {
        return casNumber;
    }

    /**
     * Sets the value of the casNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCasNumber(String value) {
        this.casNumber = value;
    }

    /**
     * Gets the value of the sumFormula property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSumFormula() {
        return sumFormula;
    }

    /**
     * Sets the value of the sumFormula property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSumFormula(String value) {
        this.sumFormula = value;
    }

    /**
     * Gets the value of the referenceFlowProperty property.
     * 
     * @return
     *     possible object is
     *     {@link ReferenceFlowProperty }
     *     
     */
    public ReferenceFlowProperty getReferenceFlowProperty() {
        return referenceFlowProperty;
    }

    /**
     * Sets the value of the referenceFlowProperty property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferenceFlowProperty }
     *     
     */
    public void setReferenceFlowProperty(ReferenceFlowProperty value) {
        this.referenceFlowProperty = value;
    }

    /**
     * Gets the value of the href property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHref(String value) {
        this.href = value;
    }

    /**
     * Gets the value of the sourceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * Sets the value of the sourceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceId(String value) {
        this.sourceId = value;
    }

}
