package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.math.BigDecimal;
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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExchangeType", propOrder = { "flow", "location",
		"exchangeFunction", "exchangeDirection", "parameterName", "meanAmount",
		"resultingAmount", "minimumAmount", "maximumAmount",
		"uncertaintyDistribution", "relativeStandardDeviation95In",
		"allocation", "dataSourceType", "dataDerivation", "dataSources",
		"generalComment", "other" })
public class Exchange implements Serializable {

	private final static long serialVersionUID = 1L;

	/** The data set internal ID (dataSetInternalID) of the exchange. */
	@XmlAttribute(name = "dataSetInternalID", required = true)
	public int id;

	@XmlElement(name = "referenceToFlowDataSet", required = true)
	public DataSetReference flow;

	public String location;

	@XmlElement(name = "functionType")
	public ExchangeFunction exchangeFunction;

	public ExchangeDirection exchangeDirection;

	@XmlElement(name = "referenceToVariable")
	public String parameterName;

	public double meanAmount;

	public Double resultingAmount;

	public Double minimumAmount;

	public Double maximumAmount;

	@XmlElement(name = "uncertaintyDistributionType")
	public UncertaintyDistribution uncertaintyDistribution;

	public BigDecimal relativeStandardDeviation95In;

	@XmlElement(name = "allocations")
	public Allocation allocation;

	public String dataSourceType;

	@XmlElement(name = "dataDerivationTypeStatus")
	public DataDerivation dataDerivation;

	@XmlElement(name = "referencesToDataSource")
	public DataSourceReferenceList dataSources;

	public final List<Label> generalComment = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public Map<QName, String> otherAttributes = new HashMap<>();

}
