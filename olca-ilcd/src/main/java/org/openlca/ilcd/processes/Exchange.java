package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.openlca.ilcd.commons.DataDerivation;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.commons.ExchangeFunction;
import org.openlca.ilcd.commons.Label;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.UncertaintyDistribution;

/**
 * <p>
 * Java class for ExchangeType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ExchangeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="referenceToFlowDataSet" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType"/>
 *         &lt;element name="location" type="{http://lca.jrc.it/ILCD/Common}String" minOccurs="0"/>
 *         &lt;element name="functionType" type="{http://lca.jrc.it/ILCD/Common}ExchangeFunctionTypeValues" minOccurs="0"/>
 *         &lt;element name="exchangeDirection" type="{http://lca.jrc.it/ILCD/Common}ExchangeDirectionValues" minOccurs="0"/>
 *         &lt;element name="referenceToVariable" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="meanAmount" type="{http://lca.jrc.it/ILCD/Common}Real"/>
 *         &lt;element name="resultingAmount" type="{http://lca.jrc.it/ILCD/Common}Real" minOccurs="0"/>
 *         &lt;element name="minimumAmount" type="{http://lca.jrc.it/ILCD/Common}Real" minOccurs="0"/>
 *         &lt;element name="maximumAmount" type="{http://lca.jrc.it/ILCD/Common}Real" minOccurs="0"/>
 *         &lt;element name="uncertaintyDistributionType" type="{http://lca.jrc.it/ILCD/Common}UncertaintyDistributionTypeValues" minOccurs="0"/>
 *         &lt;element name="relativeStandardDeviation95In" type="{http://lca.jrc.it/ILCD/Common}Perc" minOccurs="0"/>
 *         &lt;element name="allocations" type="{http://lca.jrc.it/ILCD/Process}AllocationsType" minOccurs="0"/>
 *         &lt;element name="dataSourceType" type="{http://lca.jrc.it/ILCD/Common}DataSourceTypeValues" minOccurs="0"/>
 *         &lt;element name="dataDerivationTypeStatus" type="{http://lca.jrc.it/ILCD/Common}DataDerivationTypeStatusValues" minOccurs="0"/>
 *         &lt;element name="referencesToDataSource" type="{http://lca.jrc.it/ILCD/Process}ReferencesToDataSourceType" minOccurs="0"/>
 *         &lt;element name="generalComment" type="{http://lca.jrc.it/ILCD/Common}StringMultiLang" maxOccurs="100" minOccurs="0"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}other" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="dataSetInternalID" use="required" type="{http://lca.jrc.it/ILCD/Common}Int6" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExchangeType", propOrder = { "flow", "location",
		"exchangeFunction", "exchangeDirection", "parameterName", "meanAmount",
		"resultingAmount", "minimumAmount", "maximumAmount",
		"uncertaintyDistribution", "relativeStandardDeviation95In",
		"allocation", "dataSourceType", "dataDerivation", "dataSources",
		"generalComment", "other" })
public class Exchange implements Serializable {

	private final static long serialVersionUID = 1L;
	@XmlElement(name = "referenceToFlowDataSet", required = true)
	protected DataSetReference flow;
	protected String location;
	@XmlElement(name = "functionType")
	protected ExchangeFunction exchangeFunction;
	protected ExchangeDirection exchangeDirection;
	@XmlElement(name = "referenceToVariable")
	protected String parameterName;
	protected double meanAmount;
	protected Double resultingAmount;
	protected Double minimumAmount;
	protected Double maximumAmount;
	@XmlElement(name = "uncertaintyDistributionType")
	protected UncertaintyDistribution uncertaintyDistribution;
	protected BigDecimal relativeStandardDeviation95In;
	@XmlElement(name = "allocations")
	protected Allocation allocation;
	protected String dataSourceType;
	@XmlElement(name = "dataDerivationTypeStatus")
	protected DataDerivation dataDerivation;
	@XmlElement(name = "referencesToDataSource")
	protected DataSourceReferenceList dataSources;
	protected List<Label> generalComment;
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected Other other;
	@XmlAttribute(name = "dataSetInternalID", required = true)
	protected BigInteger dataSetInternalID;
	@XmlAnyAttribute
	private Map<QName, String> otherAttributes = new HashMap<>();

	/**
	 * Gets the value of the flow property.
	 * 
	 * @return possible object is {@link DataSetReference }
	 * 
	 */
	public DataSetReference getFlow() {
		return flow;
	}

	/**
	 * Sets the value of the flow property.
	 * 
	 * @param value
	 *            allowed object is {@link DataSetReference }
	 * 
	 */
	public void setFlow(DataSetReference value) {
		this.flow = value;
	}

	/**
	 * Gets the value of the location property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Sets the value of the location property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLocation(String value) {
		this.location = value;
	}

	/**
	 * Gets the value of the exchangeFunction property.
	 * 
	 * @return possible object is {@link ExchangeFunction }
	 * 
	 */
	public ExchangeFunction getExchangeFunction() {
		return exchangeFunction;
	}

	/**
	 * Sets the value of the exchangeFunction property.
	 * 
	 * @param value
	 *            allowed object is {@link ExchangeFunction }
	 * 
	 */
	public void setExchangeFunction(ExchangeFunction value) {
		this.exchangeFunction = value;
	}

	/**
	 * Gets the value of the exchangeDirection property.
	 * 
	 * @return possible object is {@link ExchangeDirection }
	 * 
	 */
	public ExchangeDirection getExchangeDirection() {
		return exchangeDirection;
	}

	/**
	 * Sets the value of the exchangeDirection property.
	 * 
	 * @param value
	 *            allowed object is {@link ExchangeDirection }
	 * 
	 */
	public void setExchangeDirection(ExchangeDirection value) {
		this.exchangeDirection = value;
	}

	/**
	 * Gets the value of the parameterName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getParameterName() {
		return parameterName;
	}

	/**
	 * Sets the value of the parameterName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setParameterName(String value) {
		this.parameterName = value;
	}

	/**
	 * Gets the value of the meanAmount property.
	 * 
	 */
	public double getMeanAmount() {
		return meanAmount;
	}

	/**
	 * Sets the value of the meanAmount property.
	 * 
	 */
	public void setMeanAmount(double value) {
		this.meanAmount = value;
	}

	/**
	 * Gets the value of the resultingAmount property.
	 * 
	 * @return possible object is {@link Double }
	 * 
	 */
	public Double getResultingAmount() {
		return resultingAmount;
	}

	/**
	 * Sets the value of the resultingAmount property.
	 * 
	 * @param value
	 *            allowed object is {@link Double }
	 * 
	 */
	public void setResultingAmount(Double value) {
		this.resultingAmount = value;
	}

	/**
	 * Gets the value of the minimumAmount property.
	 * 
	 * @return possible object is {@link Double }
	 * 
	 */
	public Double getMinimumAmount() {
		return minimumAmount;
	}

	/**
	 * Sets the value of the minimumAmount property.
	 * 
	 * @param value
	 *            allowed object is {@link Double }
	 * 
	 */
	public void setMinimumAmount(Double value) {
		this.minimumAmount = value;
	}

	/**
	 * Gets the value of the maximumAmount property.
	 * 
	 * @return possible object is {@link Double }
	 * 
	 */
	public Double getMaximumAmount() {
		return maximumAmount;
	}

	/**
	 * Sets the value of the maximumAmount property.
	 * 
	 * @param value
	 *            allowed object is {@link Double }
	 * 
	 */
	public void setMaximumAmount(Double value) {
		this.maximumAmount = value;
	}

	/**
	 * Gets the value of the uncertaintyDistribution property.
	 * 
	 * @return possible object is {@link UncertaintyDistribution }
	 * 
	 */
	public UncertaintyDistribution getUncertaintyDistribution() {
		return uncertaintyDistribution;
	}

	/**
	 * Sets the value of the uncertaintyDistribution property.
	 * 
	 * @param value
	 *            allowed object is {@link UncertaintyDistribution }
	 * 
	 */
	public void setUncertaintyDistribution(UncertaintyDistribution value) {
		this.uncertaintyDistribution = value;
	}

	/**
	 * Gets the value of the relativeStandardDeviation95In property.
	 * 
	 * @return possible object is {@link BigDecimal }
	 * 
	 */
	public BigDecimal getRelativeStandardDeviation95In() {
		return relativeStandardDeviation95In;
	}

	/**
	 * Sets the value of the relativeStandardDeviation95In property.
	 * 
	 * @param value
	 *            allowed object is {@link BigDecimal }
	 * 
	 */
	public void setRelativeStandardDeviation95In(BigDecimal value) {
		this.relativeStandardDeviation95In = value;
	}

	/**
	 * Gets the value of the allocation property.
	 * 
	 * @return possible object is {@link Allocation }
	 * 
	 */
	public Allocation getAllocation() {
		return allocation;
	}

	/**
	 * Sets the value of the allocation property.
	 * 
	 * @param value
	 *            allowed object is {@link Allocation }
	 * 
	 */
	public void setAllocation(Allocation value) {
		this.allocation = value;
	}

	/**
	 * Gets the value of the dataSourceType property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDataSourceType() {
		return dataSourceType;
	}

	/**
	 * Sets the value of the dataSourceType property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDataSourceType(String value) {
		this.dataSourceType = value;
	}

	/**
	 * Gets the value of the dataDerivation property.
	 * 
	 * @return possible object is {@link DataDerivation }
	 * 
	 */
	public DataDerivation getDataDerivation() {
		return dataDerivation;
	}

	/**
	 * Sets the value of the dataDerivation property.
	 * 
	 * @param value
	 *            allowed object is {@link DataDerivation }
	 * 
	 */
	public void setDataDerivation(DataDerivation value) {
		this.dataDerivation = value;
	}

	/**
	 * Gets the value of the dataSources property.
	 * 
	 * @return possible object is {@link DataSourceReferenceList }
	 * 
	 */
	public DataSourceReferenceList getDataSources() {
		return dataSources;
	}

	/**
	 * Sets the value of the dataSources property.
	 * 
	 * @param value
	 *            allowed object is {@link DataSourceReferenceList }
	 * 
	 */
	public void setDataSources(DataSourceReferenceList value) {
		this.dataSources = value;
	}

	/**
	 * Gets the value of the generalComment property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the generalComment property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getGeneralComment().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Label }
	 * 
	 * 
	 */
	public List<Label> getGeneralComment() {
		if (generalComment == null) {
			generalComment = new ArrayList<>();
		}
		return this.generalComment;
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
	 * Gets the value of the dataSetInternalID property.
	 * 
	 * @return possible object is {@link BigInteger }
	 * 
	 */
	public BigInteger getDataSetInternalID() {
		return dataSetInternalID;
	}

	/**
	 * Sets the value of the dataSetInternalID property.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	public void setDataSetInternalID(BigInteger value) {
		this.dataSetInternalID = value;
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
