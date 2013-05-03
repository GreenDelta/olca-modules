package org.openlca.ecospold.internal.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ecospold.IReferenceFunction;

/**
 * Contains the identifying information of a dataset including name (english and
 * german), unit, classification (category, subCategory), etc..
 * 
 * <p>
 * Java class for TReferenceFunction complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="TReferenceFunction">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="synonym" type="{http://www.EcoInvent.org/EcoSpold01}TString80"/>
 *       &lt;/sequence>
 *       &lt;attribute name="datasetRelatesToProduct" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="name" use="required" type="{http://www.EcoInvent.org/EcoSpold01}TString80" />
 *       &lt;attribute name="localName" use="required" type="{http://www.EcoInvent.org/EcoSpold01}TString80" />
 *       &lt;attribute name="infrastructureProcess" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="amount" use="required" type="{http://www.EcoInvent.org/EcoSpold01}TFloatNumber" />
 *       &lt;attribute name="unit" use="required" type="{http://www.EcoInvent.org/EcoSpold01}TUnit" />
 *       &lt;attribute name="category" use="required" type="{http://www.EcoInvent.org/EcoSpold01}TCategoryName" />
 *       &lt;attribute name="subCategory" use="required" type="{http://www.EcoInvent.org/EcoSpold01}TCategoryName" />
 *       &lt;attribute name="localCategory" use="required" type="{http://www.EcoInvent.org/EcoSpold01}TCategoryName" />
 *       &lt;attribute name="localSubCategory" use="required" type="{http://www.EcoInvent.org/EcoSpold01}TCategoryName" />
 *       &lt;attribute name="includedProcesses" type="{http://www.EcoInvent.org/EcoSpold01}TString32000" />
 *       &lt;attribute name="generalComment" type="{http://www.EcoInvent.org/EcoSpold01}TString32000" />
 *       &lt;attribute name="infrastructureIncluded" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="CASNumber">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;maxLength value="11"/>
 *             &lt;pattern value="\d{1,6}-\d{2,2}-\d"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="statisticalClassification">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}long">
 *             &lt;pattern value="\d{1,8}"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="formula" type="{http://www.EcoInvent.org/EcoSpold01}TString40" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TReferenceFunction", propOrder = { "synonym" })
class ReferenceFunction implements Serializable, IReferenceFunction {

	private final static long serialVersionUID = 1L;
	protected List<String> synonym;
	@XmlAttribute(name = "datasetRelatesToProduct", required = true)
	protected boolean datasetRelatesToProduct;
	@XmlAttribute(name = "name", required = true)
	protected String name;
	@XmlAttribute(name = "localName", required = true)
	protected String localName;
	@XmlAttribute(name = "infrastructureProcess", required = true)
	protected boolean infrastructureProcess;
	@XmlAttribute(name = "amount", required = true)
	protected double amount;
	@XmlAttribute(name = "unit", required = true)
	protected String unit;
	@XmlAttribute(name = "category", required = true)
	protected String category;
	@XmlAttribute(name = "subCategory", required = true)
	protected String subCategory;
	@XmlAttribute(name = "localCategory", required = true)
	protected String localCategory;
	@XmlAttribute(name = "localSubCategory", required = true)
	protected String localSubCategory;
	@XmlAttribute(name = "includedProcesses")
	protected String includedProcesses;
	@XmlAttribute(name = "generalComment")
	protected String generalComment;
	@XmlAttribute(name = "infrastructureIncluded")
	protected Boolean infrastructureIncluded;
	@XmlAttribute(name = "CASNumber")
	protected String casNumber;
	@XmlAttribute(name = "statisticalClassification")
	protected Long statisticalClassification;
	@XmlAttribute(name = "formula")
	protected String formula;

	/**
	 * Gets the value of the synonym property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the synonym property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getSynonym().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link String }
	 * 
	 * 
	 */
	@Override
	public List<String> getSynonym() {
		if (synonym == null) {
			synonym = new ArrayList<>();
		}
		return this.synonym;
	}

	/**
	 * Gets the value of the datasetRelatesToProduct property.
	 * 
	 */
	@Override
	public boolean isDatasetRelatesToProduct() {
		return datasetRelatesToProduct;
	}

	/**
	 * Sets the value of the datasetRelatesToProduct property.
	 * 
	 */
	@Override
	public void setDatasetRelatesToProduct(boolean value) {
		this.datasetRelatesToProduct = value;
	}

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setName(String value) {
		this.name = value;
	}

	/**
	 * Gets the value of the localName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getLocalName() {
		return localName;
	}

	/**
	 * Sets the value of the localName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setLocalName(String value) {
		this.localName = value;
	}

	/**
	 * Gets the value of the infrastructureProcess property.
	 * 
	 */
	@Override
	public boolean isInfrastructureProcess() {
		return infrastructureProcess;
	}

	/**
	 * Sets the value of the infrastructureProcess property.
	 * 
	 */
	@Override
	public void setInfrastructureProcess(boolean value) {
		this.infrastructureProcess = value;
	}

	/**
	 * Gets the value of the amount property.
	 * 
	 */
	@Override
	public double getAmount() {
		return amount;
	}

	/**
	 * Sets the value of the amount property.
	 * 
	 */
	@Override
	public void setAmount(double value) {
		this.amount = value;
	}

	/**
	 * Gets the value of the unit property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getUnit() {
		return unit;
	}

	/**
	 * Sets the value of the unit property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setUnit(String value) {
		this.unit = value;
	}

	/**
	 * Gets the value of the category property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getCategory() {
		return category;
	}

	/**
	 * Sets the value of the category property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setCategory(String value) {
		this.category = value;
	}

	/**
	 * Gets the value of the subCategory property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getSubCategory() {
		return subCategory;
	}

	/**
	 * Sets the value of the subCategory property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setSubCategory(String value) {
		this.subCategory = value;
	}

	/**
	 * Gets the value of the localCategory property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getLocalCategory() {
		return localCategory;
	}

	/**
	 * Sets the value of the localCategory property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setLocalCategory(String value) {
		this.localCategory = value;
	}

	/**
	 * Gets the value of the localSubCategory property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getLocalSubCategory() {
		return localSubCategory;
	}

	/**
	 * Sets the value of the localSubCategory property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setLocalSubCategory(String value) {
		this.localSubCategory = value;
	}

	/**
	 * Gets the value of the includedProcesses property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getIncludedProcesses() {
		return includedProcesses;
	}

	/**
	 * Sets the value of the includedProcesses property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setIncludedProcesses(String value) {
		this.includedProcesses = value;
	}

	/**
	 * Gets the value of the generalComment property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getGeneralComment() {
		return generalComment;
	}

	/**
	 * Sets the value of the generalComment property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setGeneralComment(String value) {
		this.generalComment = value;
	}

	/**
	 * Gets the value of the infrastructureIncluded property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	@Override
	public boolean isInfrastructureIncluded() {
		if (infrastructureIncluded == null)
			return true;
		return infrastructureIncluded;
	}

	/**
	 * Sets the value of the infrastructureIncluded property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	@Override
	public void setInfrastructureIncluded(Boolean value) {
		this.infrastructureIncluded = value;
	}

	/**
	 * Gets the value of the casNumber property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getCASNumber() {
		return casNumber;
	}

	/**
	 * Sets the value of the casNumber property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setCASNumber(String value) {
		this.casNumber = value;
	}

	/**
	 * Gets the value of the statisticalClassification property.
	 * 
	 * @return possible object is {@link Long }
	 * 
	 */
	@Override
	public Long getStatisticalClassification() {
		return statisticalClassification;
	}

	/**
	 * Sets the value of the statisticalClassification property.
	 * 
	 * @param value
	 *            allowed object is {@link Long }
	 * 
	 */
	@Override
	public void setStatisticalClassification(Long value) {
		this.statisticalClassification = value;
	}

	/**
	 * Gets the value of the formula property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getFormula() {
		return formula;
	}

	/**
	 * Sets the value of the formula property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setFormula(String value) {
		this.formula = value;
	}

}
