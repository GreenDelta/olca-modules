
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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.FreeText;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.PublicationStatus;


/**
 * <p>Java class for PublicationAndOwnershipType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PublicationAndOwnershipType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}dateOfLastRevision" minOccurs="0"/>
 *         &lt;group ref="{http://lca.jrc.it/ILCD/Common}PublicationAndOwnershipGroup1"/>
 *         &lt;group ref="{http://lca.jrc.it/ILCD/Common}PublicationAndOwnershipGroup2"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}referenceToOwnershipOfDataSet" minOccurs="0"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}copyright" minOccurs="0"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}accessRestrictions" maxOccurs="100" minOccurs="0"/>
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
@XmlType(name = "PublicationAndOwnershipType", propOrder = {
    "dateOfLastRevision",
    "dataSetVersion",
    "referenceToPrecedingDataSetVersion",
    "permanentDataSetURI",
    "workflowAndPublicationStatus",
    "referenceToUnchangedRepublication",
    "referenceToOwnershipOfDataSet",
    "copyright",
    "accessRestrictions",
    "other"
})
public class Publication
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected XMLGregorianCalendar dateOfLastRevision;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", required = true)
    protected String dataSetVersion;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected List<DataSetReference> referenceToPrecedingDataSetVersion;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    @XmlSchemaType(name = "anyURI")
    protected String permanentDataSetURI;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected PublicationStatus workflowAndPublicationStatus;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected DataSetReference referenceToUnchangedRepublication;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected DataSetReference referenceToOwnershipOfDataSet;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected Boolean copyright;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected List<FreeText> accessRestrictions;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected Other other;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes =  new HashMap<>();

    /**
     * Date when the data set was revised for the last time, typically manually set.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateOfLastRevision() {
        return dateOfLastRevision;
    }

    /**
     * Sets the value of the dateOfLastRevision property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateOfLastRevision(XMLGregorianCalendar value) {
        this.dateOfLastRevision = value;
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
     * Gets the value of the referenceToPrecedingDataSetVersion property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the referenceToPrecedingDataSetVersion property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferenceToPrecedingDataSetVersion().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataSetReference }
     * 
     * 
     */
    public List<DataSetReference> getReferenceToPrecedingDataSetVersion() {
        if (referenceToPrecedingDataSetVersion == null) {
            referenceToPrecedingDataSetVersion = new ArrayList<>();
        }
        return this.referenceToPrecedingDataSetVersion;
    }

    /**
     * Gets the value of the permanentDataSetURI property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPermanentDataSetURI() {
        return permanentDataSetURI;
    }

    /**
     * Sets the value of the permanentDataSetURI property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPermanentDataSetURI(String value) {
        this.permanentDataSetURI = value;
    }

    /**
     * Gets the value of the workflowAndPublicationStatus property.
     * 
     * @return
     *     possible object is
     *     {@link PublicationStatus }
     *     
     */
    public PublicationStatus getWorkflowAndPublicationStatus() {
        return workflowAndPublicationStatus;
    }

    /**
     * Sets the value of the workflowAndPublicationStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link PublicationStatus }
     *     
     */
    public void setWorkflowAndPublicationStatus(PublicationStatus value) {
        this.workflowAndPublicationStatus = value;
    }

    /**
     * Gets the value of the referenceToUnchangedRepublication property.
     * 
     * @return
     *     possible object is
     *     {@link DataSetReference }
     *     
     */
    public DataSetReference getReferenceToUnchangedRepublication() {
        return referenceToUnchangedRepublication;
    }

    /**
     * Sets the value of the referenceToUnchangedRepublication property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataSetReference }
     *     
     */
    public void setReferenceToUnchangedRepublication(DataSetReference value) {
        this.referenceToUnchangedRepublication = value;
    }

    /**
     * "Contact data set" of the person or entity who owns this data set. (Note: this is not necessarily the publisher of the data set.)
     * 
     * @return
     *     possible object is
     *     {@link DataSetReference }
     *     
     */
    public DataSetReference getReferenceToOwnershipOfDataSet() {
        return referenceToOwnershipOfDataSet;
    }

    /**
     * Sets the value of the referenceToOwnershipOfDataSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataSetReference }
     *     
     */
    public void setReferenceToOwnershipOfDataSet(DataSetReference value) {
        this.referenceToOwnershipOfDataSet = value;
    }

    /**
     * Indicates whether or not a copyright on the data set exists. Decided upon by the "Owner of data set". [Note: See also field "Access and use
     *             restrictions".]
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCopyright() {
        return copyright;
    }

    /**
     * Sets the value of the copyright property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCopyright(Boolean value) {
        this.copyright = value;
    }

    /**
     * Access restrictions / use conditions for this data set as free text or referring to e.g. license conditions. In case of no restrictions "None" is
     *             entered.Gets the value of the accessRestrictions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the accessRestrictions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAccessRestrictions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FreeText }
     * 
     * 
     */
    public List<FreeText> getAccessRestrictions() {
        if (accessRestrictions == null) {
            accessRestrictions = new ArrayList<>();
        }
        return this.accessRestrictions;
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
