package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for QuantitativeReferenceType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="QuantitativeReferenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="referenceFlow" type="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}ReferenceFlowType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="functionalUnit" type="{http://www.ilcd-network.org/ILCD/ServiceAPI}StringMultiLang" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" type="{http://www.ilcd-network.org/ILCD/ServiceAPI}TypeOfQuantitativeReferenceValues" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuantitativeReferenceType", propOrder = { "referenceFlow",
		"functionalUnit" })
public class QuantitativeReferenceType implements Serializable {

	private final static long serialVersionUID = 1L;
	protected List<ReferenceFlowType> referenceFlow;
	protected List<LangString> functionalUnit;
	@XmlAttribute(name = "type")
	protected TypeOfQuantitativeReferenceValues type;

	/**
	 * Gets the value of the referenceFlow property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the referenceFlow property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getReferenceFlow().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ReferenceFlowType }
	 * 
	 * 
	 */
	public List<ReferenceFlowType> getReferenceFlow() {
		if (referenceFlow == null) {
			referenceFlow = new ArrayList<>();
		}
		return this.referenceFlow;
	}

	/**
	 * Gets the value of the functionalUnit property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the functionalUnit property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getFunctionalUnit().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link LangString }
	 * 
	 * 
	 */
	public List<LangString> getFunctionalUnit() {
		if (functionalUnit == null) {
			functionalUnit = new ArrayList<>();
		}
		return this.functionalUnit;
	}

	/**
	 * Gets the value of the type property.
	 * 
	 * @return possible object is {@link TypeOfQuantitativeReferenceValues }
	 * 
	 */
	public TypeOfQuantitativeReferenceValues getType() {
		return type;
	}

	/**
	 * Sets the value of the type property.
	 * 
	 * @param value
	 *            allowed object is {@link TypeOfQuantitativeReferenceValues }
	 * 
	 */
	public void setType(TypeOfQuantitativeReferenceValues value) {
		this.type = value;
	}

}
