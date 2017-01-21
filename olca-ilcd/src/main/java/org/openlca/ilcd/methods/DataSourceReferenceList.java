
package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Ref;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReferencesToDataSourceType", propOrder = {
		"referenceToDataSource",
		"other"
})
public class DataSourceReferenceList
		implements Serializable {

	private final static long serialVersionUID = 1L;
	protected Ref referenceToDataSource;
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected Other other;
	@XmlAnyAttribute
	private Map<QName, String> otherAttributes = new HashMap<>();

	/**
	 * Gets the value of the referenceToDataSource property.
	 * 
	 * @return possible object is {@link Ref }
	 * 
	 */
	public Ref getReferenceToDataSource() {
		return referenceToDataSource;
	}

	/**
	 * Sets the value of the referenceToDataSource property.
	 * 
	 * @param value
	 *            allowed object is {@link Ref }
	 * 
	 */
	public void setReferenceToDataSource(Ref value) {
		this.referenceToDataSource = value;
	}

	/**
	 * Gets the value of the other property.
	 * 
	 * @return possible object is {@link Other }
	 * 
	 */
	public Other getOther() {
		return other;
	}

	/**
	 * Sets the value of the other property.
	 * 
	 * @param value
	 *            allowed object is {@link Other }
	 * 
	 */
	public void setOther(Other value) {
		this.other = value;
	}

	/**
	 * Gets a map that contains attributes that aren't bound to any typed
	 * property on this class.
	 * 
	 * <p>
	 * the map is keyed by the name of the attribute and the value is the string
	 * value of the attribute.
	 * 
	 * the map returned by this method is live, and you can add new attribute by
	 * updating the map directly. Because of this design, there's no setter.
	 * 
	 * 
	 * @return always non-null
	 */
	public Map<QName, String> getOtherAttributes() {
		return otherAttributes;
	}

}
