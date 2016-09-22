
package org.openlca.ilcd.flows;

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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FlowInformationType", propOrder = {
		"dataSetInformation",
		"quantitativeReference",
		"geography",
		"technology",
		"other"
})
public class FlowInfo
		implements Serializable {

	private final static long serialVersionUID = 1L;
	@XmlElement(required = true)
	protected DataSetInfo dataSetInformation;
	protected QuantitativeReference quantitativeReference;
	protected Geography geography;
	protected Technology technology;
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected Other other;
	@XmlAnyAttribute
	private Map<QName, String> otherAttributes = new HashMap<>();

	/**
	 * Gets the value of the dataSetInformation property.
	 * 
	 * @return possible object is {@link DataSetInfo }
	 * 
	 */
	public DataSetInfo getDataSetInformation() {
		return dataSetInformation;
	}

	/**
	 * Sets the value of the dataSetInformation property.
	 * 
	 * @param value
	 *            allowed object is {@link DataSetInfo }
	 * 
	 */
	public void setDataSetInformation(DataSetInfo value) {
		this.dataSetInformation = value;
	}

	/**
	 * Gets the value of the quantitativeReference property.
	 * 
	 * @return possible object is {@link QuantitativeReference }
	 * 
	 */
	public QuantitativeReference getQuantitativeReference() {
		return quantitativeReference;
	}

	/**
	 * Sets the value of the quantitativeReference property.
	 * 
	 * @param value
	 *            allowed object is {@link QuantitativeReference }
	 * 
	 */
	public void setQuantitativeReference(QuantitativeReference value) {
		this.quantitativeReference = value;
	}

	/**
	 * Gets the value of the geography property.
	 * 
	 * @return possible object is {@link Geography }
	 * 
	 */
	public Geography getGeography() {
		return geography;
	}

	/**
	 * Sets the value of the geography property.
	 * 
	 * @param value
	 *            allowed object is {@link Geography }
	 * 
	 */
	public void setGeography(Geography value) {
		this.geography = value;
	}

	/**
	 * Gets the value of the technology property.
	 * 
	 * @return possible object is {@link Technology }
	 * 
	 */
	public Technology getTechnology() {
		return technology;
	}

	/**
	 * Sets the value of the technology property.
	 * 
	 * @param value
	 *            allowed object is {@link Technology }
	 * 
	 */
	public void setTechnology(Technology value) {
		this.technology = value;
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
