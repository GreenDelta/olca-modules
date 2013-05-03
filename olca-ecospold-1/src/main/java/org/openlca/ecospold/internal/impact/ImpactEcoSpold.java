package org.openlca.ecospold.internal.impact;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ecospold.IDataSet;
import org.openlca.ecospold.IEcoSpold;
import org.w3c.dom.Element;

/**
 * The data (exchange) format of the ECOINVENT quality network.
 * 
 * <p>
 * Java class for TEcoSpold complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="TEcoSpold">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dataset" type="{http://www.EcoInvent.org/EcoSpold01Impact}TDataset" maxOccurs="unbounded"/>
 *         &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="validationId" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="validationStatus" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TEcoSpold", propOrder = { "dataset", "any" })
class ImpactEcoSpold implements Serializable, IEcoSpold {

	private final static long serialVersionUID = 1L;
	@XmlElement(required = true, type = ImpactDataSet.class)
	protected List<IDataSet> dataset;
	@XmlAnyElement(lax = true)
	protected List<Object> any;
	@XmlAttribute(name = "validationId")
	protected BigInteger validationId;
	@XmlAttribute(name = "validationStatus")
	protected String validationStatus;

	/**
	 * Gets the value of the dataset property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the dataset property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDataset().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ImpactDataSet }
	 * 
	 * 
	 */
	@Override
	public List<IDataSet> getDataset() {
		if (dataset == null) {
			dataset = new ArrayList<>();
		}
		return this.dataset;
	}

	/**
	 * Gets the value of the any property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the any property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getAny().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Object }
	 * {@link Element }
	 * 
	 * 
	 */
	@Override
	public List<Object> getAny() {
		if (any == null) {
			any = new ArrayList<>();
		}
		return this.any;
	}

	/**
	 * Gets the value of the validationId property.
	 * 
	 * @return possible object is {@link BigInteger }
	 * 
	 */
	@Override
	public BigInteger getValidationId() {
		return validationId;
	}

	/**
	 * Sets the value of the validationId property.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	@Override
	public void setValidationId(BigInteger value) {
		this.validationId = value;
	}

	/**
	 * Gets the value of the validationStatus property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getValidationStatus() {
		return validationStatus;
	}

	/**
	 * Sets the value of the validationStatus property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setValidationStatus(String value) {
		this.validationStatus = value;
	}

}
