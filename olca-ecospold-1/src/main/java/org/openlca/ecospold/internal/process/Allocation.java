package org.openlca.ecospold.internal.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ecospold.IAllocation;

/**
 * Contains all information about allocation procedure, allocation parameters
 * and allocation factors applied on a multi-output process.
 * 
 * <p>
 * Java class for TAllocation complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="TAllocation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="referenceToInputOutput" type="{http://www.EcoInvent.org/EcoSpold01}TIndexNumber"/>
 *       &lt;/sequence>
 *       &lt;attribute name="referenceToCoProduct" use="required" type="{http://www.EcoInvent.org/EcoSpold01}TIndexNumber" />
 *       &lt;attribute name="allocationMethod" default="-1">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *             &lt;minInclusive value="-1"/>
 *             &lt;maxInclusive value="2"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="fraction" use="required" type="{http://www.w3.org/2001/XMLSchema}float" />
 *       &lt;attribute name="explanations" type="{http://www.EcoInvent.org/EcoSpold01}TString32000" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TAllocation", propOrder = { "referenceToInputOutput" })
class Allocation implements Serializable, IAllocation {

	private final static long serialVersionUID = 1L;
	@XmlElement(type = Integer.class)
	protected List<Integer> referenceToInputOutput;
	@XmlAttribute(name = "referenceToCoProduct", required = true)
	protected int referenceToCoProduct;
	@XmlAttribute(name = "allocationMethod")
	protected Integer allocationMethod;
	@XmlAttribute(name = "fraction", required = true)
	protected float fraction;
	@XmlAttribute(name = "explanations")
	protected String explanations;

	/**
	 * Gets the value of the referenceToInputOutput property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the referenceToInputOutput property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getReferenceToInputOutput().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Integer }
	 * 
	 * 
	 */
	@Override
	public List<Integer> getReferenceToInputOutput() {
		if (referenceToInputOutput == null) {
			referenceToInputOutput = new ArrayList<>();
		}
		return this.referenceToInputOutput;
	}

	/**
	 * Gets the value of the referenceToCoProduct property.
	 * 
	 */
	@Override
	public int getReferenceToCoProduct() {
		return referenceToCoProduct;
	}

	/**
	 * Sets the value of the referenceToCoProduct property.
	 * 
	 */
	@Override
	public void setReferenceToCoProduct(int value) {
		this.referenceToCoProduct = value;
	}

	/**
	 * Gets the value of the allocationMethod property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	@Override
	public int getAllocationMethod() {
		if (allocationMethod == null)
			return -1;
		return allocationMethod;
	}

	/**
	 * Sets the value of the allocationMethod property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	@Override
	public void setAllocationMethod(Integer value) {
		this.allocationMethod = value;
	}

	/**
	 * Gets the value of the fraction property.
	 * 
	 */
	@Override
	public float getFraction() {
		return fraction;
	}

	/**
	 * Sets the value of the fraction property.
	 * 
	 */
	@Override
	public void setFraction(float value) {
		this.fraction = value;
	}

	/**
	 * Gets the value of the explanations property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getExplanations() {
		return explanations;
	}

	/**
	 * Sets the value of the explanations property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setExplanations(String value) {
		this.explanations = value;
	}

}
