package org.openlca.ilcd.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for FlowCategoryInformationType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="FlowCategoryInformationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="elementaryFlowCategorization" type="{http://lca.jrc.it/ILCD/Common}FlowCategorizationType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="classification" type="{http://lca.jrc.it/ILCD/Common}ClassificationType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FlowCategoryInformationType", propOrder = {
		"elementaryFlowCategorizations", "classifications" })
public class FlowCategoryInformation implements Serializable {

	private final static long serialVersionUID = 1L;
	@XmlElement(name = "elementaryFlowCategorization")
	protected List<FlowCategorization> elementaryFlowCategorizations;
	@XmlElement(name = "classification")
	protected List<Classification> classifications;

	/**
	 * Gets the value of the elementaryFlowCategorizations property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the elementaryFlowCategorizations property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getElementaryFlowCategorizations().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link FlowCategorization }
	 * 
	 * 
	 */
	public List<FlowCategorization> getElementaryFlowCategorizations() {
		if (elementaryFlowCategorizations == null) {
			elementaryFlowCategorizations = new ArrayList<>();
		}
		return this.elementaryFlowCategorizations;
	}

	/**
	 * Gets the value of the classifications property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the classifications property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getClassifications().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Classification }
	 * 
	 * 
	 */
	public List<Classification> getClassifications() {
		if (classifications == null) {
			classifications = new ArrayList<>();
		}
		return this.classifications;
	}

}
