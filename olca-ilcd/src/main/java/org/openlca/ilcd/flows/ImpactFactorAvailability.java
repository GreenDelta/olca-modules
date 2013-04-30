
package org.openlca.ilcd.flows;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.openlca.ilcd.commons.Availability;
import org.openlca.ilcd.commons.ImpactCategory;


/**
 * <p>Java class for CompletenessAvailabilityImpactFactorsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CompletenessAvailabilityImpactFactorsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="type" use="required" type="{http://lca.jrc.it/ILCD/Common}CompletenessTypeValues" />
 *       &lt;attribute name="value" use="required" type="{http://lca.jrc.it/ILCD/Common}CompletenessAvailabilityValues" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompletenessAvailabilityImpactFactorsType")
public class ImpactFactorAvailability
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlAttribute(name = "type", required = true)
    protected ImpactCategory type;
    @XmlAttribute(name = "value", required = true)
    protected Availability value;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes =  new HashMap<>();

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link ImpactCategory }
     *     
     */
    public ImpactCategory getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link ImpactCategory }
     *     
     */
    public void setType(ImpactCategory value) {
        this.type = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link Availability }
     *     
     */
    public Availability getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link Availability }
     *     
     */
    public void setValue(Availability value) {
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
