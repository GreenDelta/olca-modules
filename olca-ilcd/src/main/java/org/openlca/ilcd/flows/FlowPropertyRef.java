
package org.openlca.ilcd.flows;

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

import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.FlowDataDerivation;
import org.openlca.ilcd.commons.Label;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.UncertaintyDistribution;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FlowPropertyType", propOrder = {
		"flowProperty",
		"meanValue",
		"minimumValue",
		"maximumValue",
		"uncertaintyDistribution",
		"relativeStandardDeviation95In",
		"dataDerivation",
		"generalComment",
		"other"
})
public class FlowPropertyRef implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "referenceToFlowPropertyDataSet", required = true)
	public DataSetReference flowProperty;

	public double meanValue;

	public Double minimumValue;

	public Double maximumValue;

	@XmlElement(name = "uncertaintyDistributionType")
	public UncertaintyDistribution uncertaintyDistribution;

	public BigDecimal relativeStandardDeviation95In;

	@XmlElement(name = "dataDerivationTypeStatus")
	public FlowDataDerivation dataDerivation;

	public final List<Label> generalComment = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAttribute(name = "dataSetInternalID")
	public BigInteger dataSetInternalID;

	@XmlAnyAttribute
	public Map<QName, String> otherAttributes = new HashMap<>();

}
