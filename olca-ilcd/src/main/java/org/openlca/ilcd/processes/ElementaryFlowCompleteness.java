
package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.openlca.ilcd.commons.FlowCompleteness;
import org.openlca.ilcd.commons.ImpactCategory;


/**
 * <p>Java class for CompletenessElementaryFlowsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CompletenessElementaryFlowsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="type" type="{http://lca.jrc.it/ILCD/Common}CompletenessTypeValues" />
 *       &lt;attribute name="value" type="{http://lca.jrc.it/ILCD/Common}CompletenessValues" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompletenessElementaryFlowsType")
public class ElementaryFlowCompleteness
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlAttribute(name = "type")
    protected ImpactCategory impactCategory;
    @XmlAttribute(name = "value")
    protected FlowCompleteness value;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes =  new HashMap<>();

    /**
     * Gets the value of the impactCategory property.
     * 
     * @return
     *     possible object is
     *     {@link ImpactCategory }
     *     
     */
    public ImpactCategory getImpactCategory() {
        return impactCategory;
    }

    /**
     * Sets the value of the impactCategory property.
     * 
     * @param value
     *     allowed object is
     *     {@link ImpactCategory }
     *     
     */
    public void setImpactCategory(ImpactCategory value) {
        this.impactCategory = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link FlowCompleteness }
     *     
     */
    public FlowCompleteness getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link FlowCompleteness }
     *     
     */
    public void setValue(FlowCompleteness value) {
        this.value = value;
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
