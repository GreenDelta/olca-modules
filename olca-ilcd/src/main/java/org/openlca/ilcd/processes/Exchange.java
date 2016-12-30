package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.DataDerivation;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.commons.ExchangeFunction;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.UncertaintyDistribution;
import org.openlca.ilcd.commons.annotations.Label;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExchangeType", propOrder = {
		"flow",
		"location",
		"exchangeFunction",
		"direction",
		"variable",
		"meanAmount",
		"resultingAmount",
		"minimumAmount",
		"maximumAmount",
		"uncertaintyDistribution",
		"relativeStandardDeviation95In",
		"allocations",
		"dataSourceType",
		"dataDerivation",
		"sources",
		"comment",
		"other" })
public class Exchange implements Serializable {

	private final static long serialVersionUID = 1L;

	/** The data set internal ID (dataSetInternalID) of the exchange. */
	@XmlAttribute(name = "dataSetInternalID", required = true)
	public int id;

	@XmlElement(name = "referenceToFlowDataSet", required = true)
	public Ref flow;

	public String location;

	@XmlElement(name = "functionType")
	public ExchangeFunction exchangeFunction;

	@XmlElement(name = "exchangeDirection")
	public ExchangeDirection direction;

	@XmlElement(name = "referenceToVariable")
	public String variable;

	@XmlElement(name = "meanAmount")
	public double meanAmount;

	@XmlElement(name = "resultingAmount")
	public Double resultingAmount;

	public Double minimumAmount;

	public Double maximumAmount;

	@XmlElement(name = "uncertaintyDistributionType")
	public UncertaintyDistribution uncertaintyDistribution;

	public Double relativeStandardDeviation95In;

	@XmlElementWrapper(name = "allocations")
	@XmlElement(name = "allocation", required = true)
	public AllocationFactor[] allocations;

	public String dataSourceType;

	@XmlElement(name = "dataDerivationTypeStatus")
	public DataDerivation dataDerivation;

	@XmlElementWrapper(name = "referencesToDataSource")
	@XmlElement(name = "referenceToDataSource")
	public Ref[] sources;

	@Label
	@XmlElement(name = "generalComment")
	public final List<LangString> comment = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public Exchange clone() {
		Exchange clone = new Exchange();
		clone.id = id;
		if (flow != null)
			clone.flow = flow.clone();
		clone.location = location;
		clone.exchangeFunction = exchangeFunction;
		clone.direction = direction;
		clone.variable = variable;
		clone.meanAmount = meanAmount;
		clone.resultingAmount = resultingAmount;
		clone.minimumAmount = minimumAmount;
		clone.maximumAmount = maximumAmount;
		clone.uncertaintyDistribution = uncertaintyDistribution;
		clone.relativeStandardDeviation95In = relativeStandardDeviation95In;
		clone.dataSourceType = dataSourceType;
		clone.dataDerivation = dataDerivation;
		clone.sources = Ref.copy(sources);
		LangString.copy(comment, clone.comment);
		cloneAllocations(clone);
		if (other != null)
			clone.other = other.clone();
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}

	private void cloneAllocations(Exchange clone) {
		if (allocations == null)
			return;
		clone.allocations = new AllocationFactor[allocations.length];
		for (int i = 0; i < allocations.length; i++) {
			if (allocations[i] == null)
				continue;
			clone.allocations[i] = allocations[i].clone();
		}
	}

	/** Adds the given allocation factor to this exchange. */
	public void add(AllocationFactor f) {
		if (f == null)
			return;
		if (allocations == null) {
			allocations = new AllocationFactor[] { f };
			return;
		}
		AllocationFactor[] next = new AllocationFactor[allocations.length + 1];
		System.arraycopy(allocations, 0, next, 0, allocations.length);
		next[allocations.length] = f;
		allocations = next;
	}
}
