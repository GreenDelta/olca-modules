
package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DataSetListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataSetListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}process"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Flow}flow"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/FlowProperty}flowProperty"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/UnitGroup}unitGroup"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Source}source"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Contact}contact"/>
 *       &lt;/choice>
 *       &lt;attribute ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}sourceId"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataSetListType", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI", propOrder = {
    "descriptors"
})
public class DescriptorList
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElements({
        @XmlElement(name = "process", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", type = ProcessDescriptor.class),
        @XmlElement(name = "contact", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Contact", type = ContactDescriptor.class),
        @XmlElement(name = "source", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Source", type = SourceDescriptor.class),
        @XmlElement(name = "flow", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Flow", type = FlowDescriptor.class),
        @XmlElement(name = "unitGroup", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/UnitGroup", type = UnitGroupDescriptor.class),
        @XmlElement(name = "flowProperty", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/FlowProperty", type = FlowPropertyDescriptor.class)
    })
    protected List<Serializable> descriptors;
    @XmlAttribute(name = "sourceId", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
    protected String sourceId;

    /**
     * Gets the value of the descriptors property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the descriptors property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescriptors().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProcessDescriptor }
     * {@link ContactDescriptor }
     * {@link SourceDescriptor }
     * {@link FlowDescriptor }
     * {@link UnitGroupDescriptor }
     * {@link FlowPropertyDescriptor }
     * 
     * 
     */
    public List<Serializable> getDescriptors() {
        if (descriptors == null) {
            descriptors = new ArrayList<>();
        }
        return this.descriptors;
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
