package org.openlca.ecospold.internal.impact;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ecospold.IExchange;

/**
 * Comprises all inputs and outputs (both elementary flows and intermediate
 * product flows) recorded in a unit process and its related information.
 * 
 * <p>
 * Java class for TExchange complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="TExchange">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice minOccurs="0">
 *         &lt;element name="inputGroup">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *               &lt;minInclusive value="1"/>
 *               &lt;maxInclusive value="5"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="outputGroup">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *               &lt;minInclusive value="0"/>
 *               &lt;maxInclusive value="4"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/choice>
 *       &lt;attribute name="number" use="required" type="{http://www.EcoInvent.org/EcoSpold01Impact}TIndexNumber" />
 *       &lt;attribute name="category" type="{http://www.EcoInvent.org/EcoSpold01Impact}TCategoryName" />
 *       &lt;attribute name="subCategory" type="{http://www.EcoInvent.org/EcoSpold01Impact}TCategoryName" />
 *       &lt;attribute name="localCategory" type="{http://www.EcoInvent.org/EcoSpold01Impact}TCategoryName" />
 *       &lt;attribute name="localSubCategory" type="{http://www.EcoInvent.org/EcoSpold01Impact}TCategoryName" />
 *       &lt;attribute name="CASNumber">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;length value="11"/>
 *             &lt;pattern value="\d{6,6}-\d{2,2}-\d"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="name" use="required" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString80" />
 *       &lt;attribute name="location" type="{http://www.EcoInvent.org/EcoSpold01Impact}TRegionalCode" />
 *       &lt;attribute name="unit" use="required" type="{http://www.EcoInvent.org/EcoSpold01Impact}TUnit" />
 *       &lt;attribute name="meanValue" use="required" type="{http://www.EcoInvent.org/EcoSpold01Impact}TFloatNumber" />
 *       &lt;attribute name="uncertaintyType">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *             &lt;minInclusive value="0"/>
 *             &lt;maxInclusive value="4"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="standardDeviation95" type="{http://www.EcoInvent.org/EcoSpold01Impact}TFloatNumber" />
 *       &lt;attribute name="formula" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString40" />
 *       &lt;attribute name="referenceToSource" type="{http://www.EcoInvent.org/EcoSpold01Impact}TIndexNumber" />
 *       &lt;attribute name="pageNumbers" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString30" />
 *       &lt;attribute name="generalComment" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString32000" />
 *       &lt;attribute name="localName" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString80" />
 *       &lt;attribute name="infrastructureProcess" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="minValue" type="{http://www.EcoInvent.org/EcoSpold01Impact}TFloatNumber" />
 *       &lt;attribute name="maxValue" type="{http://www.EcoInvent.org/EcoSpold01Impact}TFloatNumber" />
 *       &lt;attribute name="mostLikelyValue" type="{http://www.EcoInvent.org/EcoSpold01Impact}TFloatNumber" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TExchange", propOrder = { "inputGroup", "outputGroup" })
class ImpactFactor implements Serializable, IExchange {

	private final static long serialVersionUID = 1L;
	protected Integer inputGroup;
	protected Integer outputGroup;
	@XmlAttribute(name = "number", required = true)
	protected int number;
	@XmlAttribute(name = "category")
	protected String category;
	@XmlAttribute(name = "subCategory")
	protected String subCategory;
	@XmlAttribute(name = "localCategory")
	protected String localCategory;
	@XmlAttribute(name = "localSubCategory")
	protected String localSubCategory;
	@XmlAttribute(name = "CASNumber")
	protected String casNumber;
	@XmlAttribute(name = "name", required = true)
	protected String name;
	@XmlAttribute(name = "location")
	protected String location;
	@XmlAttribute(name = "unit", required = true)
	protected String unit;
	@XmlAttribute(name = "meanValue", required = true)
	protected double meanValue;
	@XmlAttribute(name = "uncertaintyType")
	protected Integer uncertaintyType;
	@XmlAttribute(name = "standardDeviation95")
	protected Double standardDeviation95;
	@XmlAttribute(name = "formula")
	protected String formula;
	@XmlAttribute(name = "referenceToSource")
	protected Integer referenceToSource;
	@XmlAttribute(name = "pageNumbers")
	protected String pageNumbers;
	@XmlAttribute(name = "generalComment")
	protected String generalComment;
	@XmlAttribute(name = "localName")
	protected String localName;
	@XmlAttribute(name = "infrastructureProcess")
	protected Boolean infrastructureProcess;
	@XmlAttribute(name = "minValue")
	protected Double minValue;
	@XmlAttribute(name = "maxValue")
	protected Double maxValue;
	@XmlAttribute(name = "mostLikelyValue")
	protected Double mostLikelyValue;

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
	 * Gets the value of the inputGroup property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */

	@Override
	public Integer getInputGroup() {
		return inputGroup;
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
	 * Gets the value of the location property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */

	@Override
	public String getLocation() {
		return location;
	}

	/**
	 * Gets the value of the maxValue property.
	 * 
	 * @return possible object is {@link Double }
	 * 
	 */

	@Override
	public Double getMaxValue() {
		return maxValue;
	}

	/**
	 * Gets the value of the meanValue property.
	 * 
	 */

	@Override
	public double getMeanValue() {
		return meanValue;
	}

	/**
	 * Gets the value of the minValue property.
	 * 
	 * @return possible object is {@link Double }
	 * 
	 */

	@Override
	public Double getMinValue() {
		return minValue;
	}

	/**
	 * Gets the value of the mostLikelyValue property.
	 * 
	 * @return possible object is {@link Double }
	 * 
	 */

	@Override
	public Double getMostLikelyValue() {
		return mostLikelyValue;
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
	 * Gets the value of the number property.
	 * 
	 */

	@Override
	public int getNumber() {
		return number;
	}

	/**
	 * Gets the value of the outputGroup property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */

	@Override
	public Integer getOutputGroup() {
		return outputGroup;
	}

	/**
	 * Gets the value of the pageNumbers property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */

	@Override
	public String getPageNumbers() {
		return pageNumbers;
	}

	/**
	 * Gets the value of the referenceToSource property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */

	@Override
	public Integer getReferenceToSource() {
		return referenceToSource;
	}

	/**
	 * Gets the value of the standardDeviation95 property.
	 * 
	 * @return possible object is {@link Double }
	 * 
	 */

	@Override
	public Double getStandardDeviation95() {
		return standardDeviation95;
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
	 * Gets the value of the uncertaintyType property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */

	@Override
	public Integer getUncertaintyType() {
		return uncertaintyType;
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
	 * Gets the value of the infrastructureProcess property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */

	@Override
	public Boolean isInfrastructureProcess() {
		return infrastructureProcess;
	}

	/**
	 * Sets the value of the casNumber property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */

	@Override
	public void setCASNumber(final String value) {
		this.casNumber = value;
	}

	/**
	 * Sets the value of the category property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */

	@Override
	public void setCategory(final String value) {
		this.category = value;
	}

	/**
	 * Sets the value of the formula property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */

	@Override
	public void setFormula(final String value) {
		this.formula = value;
	}

	/**
	 * Sets the value of the generalComment property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */

	@Override
	public void setGeneralComment(final String value) {
		this.generalComment = value;
	}

	/**
	 * Sets the value of the infrastructureProcess property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */

	@Override
	public void setInfrastructureProcess(final Boolean value) {
		this.infrastructureProcess = value;
	}

	/**
	 * Sets the value of the inputGroup property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */

	@Override
	public void setInputGroup(final Integer value) {
		this.inputGroup = value;
	}

	/**
	 * Sets the value of the localCategory property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */

	@Override
	public void setLocalCategory(final String value) {
		this.localCategory = value;
	}

	/**
	 * Sets the value of the localName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */

	@Override
	public void setLocalName(final String value) {
		this.localName = value;
	}

	/**
	 * Sets the value of the localSubCategory property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */

	@Override
	public void setLocalSubCategory(final String value) {
		this.localSubCategory = value;
	}

	/**
	 * Sets the value of the location property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */

	@Override
	public void setLocation(final String value) {
		this.location = value;
	}

	/**
	 * Sets the value of the maxValue property.
	 * 
	 * @param value
	 *            allowed object is {@link Double }
	 * 
	 */

	@Override
	public void setMaxValue(final Double value) {
		this.maxValue = value;
	}

	/**
	 * Sets the value of the meanValue property.
	 * 
	 */

	@Override
	public void setMeanValue(final double value) {
		this.meanValue = value;
	}

	/**
	 * Sets the value of the minValue property.
	 * 
	 * @param value
	 *            allowed object is {@link Double }
	 * 
	 */

	@Override
	public void setMinValue(final Double value) {
		this.minValue = value;
	}

	/**
	 * Sets the value of the mostLikelyValue property.
	 * 
	 * @param value
	 *            allowed object is {@link Double }
	 * 
	 */

	@Override
	public void setMostLikelyValue(final Double value) {
		this.mostLikelyValue = value;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */

	@Override
	public void setName(final String value) {
		this.name = value;
	}

	/**
	 * Sets the value of the number property.
	 * 
	 */

	@Override
	public void setNumber(final int value) {
		this.number = value;
	}

	/**
	 * Sets the value of the outputGroup property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */

	@Override
	public void setOutputGroup(final Integer value) {
		this.outputGroup = value;
	}

	/**
	 * Sets the value of the pageNumbers property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */

	@Override
	public void setPageNumbers(final String value) {
		this.pageNumbers = value;
	}

	/**
	 * Sets the value of the referenceToSource property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */

	@Override
	public void setReferenceToSource(final Integer value) {
		this.referenceToSource = value;
	}

	/**
	 * Sets the value of the standardDeviation95 property.
	 * 
	 * @param value
	 *            allowed object is {@link Double }
	 * 
	 */

	@Override
	public void setStandardDeviation95(final Double value) {
		this.standardDeviation95 = value;
	}

	/**
	 * Sets the value of the subCategory property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */

	@Override
	public void setSubCategory(final String value) {
		this.subCategory = value;
	}

	/**
	 * Sets the value of the uncertaintyType property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */

	@Override
	public void setUncertaintyType(final Integer value) {
		this.uncertaintyType = value;
	}

	/**
	 * Sets the value of the unit property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */

	@Override
	public void setUnit(final String value) {
		this.unit = value;
	}

	@Override
	public boolean isElementaryFlow() {
		if (inputGroup == null && outputGroup == null)
			return true;
		if (inputGroup != null && inputGroup == 4)
			return true;
		if (outputGroup != null && outputGroup == 4)
			return true;
		return false;
	}

}
