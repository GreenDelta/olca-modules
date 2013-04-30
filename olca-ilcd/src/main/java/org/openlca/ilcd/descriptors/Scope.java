package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}method" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.ilcd-network.org/ILCD/ServiceAPI}ScopeOfReviewValues" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "method" })
@XmlRootElement(name = "scope")
public class Scope implements Serializable {

	private final static long serialVersionUID = 1L;
	protected List<Method> method;
	@XmlAttribute(name = "name", required = true)
	protected ScopeOfReviewValues name;

	/**
	 * Gets the value of the method property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the method property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getMethod().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Method }
	 * 
	 * 
	 */
	public List<Method> getMethod() {
		if (method == null) {
			method = new ArrayList<>();
		}
		return this.method;
	}

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link ScopeOfReviewValues }
	 * 
	 */
	public ScopeOfReviewValues getName() {
		return name;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link ScopeOfReviewValues }
	 * 
	 */
	public void setName(ScopeOfReviewValues value) {
		this.name = value;
	}

}
