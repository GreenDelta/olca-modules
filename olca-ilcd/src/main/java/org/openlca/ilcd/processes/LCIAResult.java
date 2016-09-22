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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.Label;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.UncertaintyDistribution;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LCIAResultType", propOrder = {
		"lciaMethod",
		"meanAmount",
		"uncertaintyDistribution",
		"relativeStandardDeviation95In",
		"generalComment",
		"other"
})
public class LCIAResult implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "referenceToLCIAMethodDataSet", required = true)
	public DataSetReference lciaMethod;

	public double meanAmount;

	@XmlElement(name = "uncertaintyDistributionType")
	public UncertaintyDistribution uncertaintyDistribution;

	public BigDecimal relativeStandardDeviation95In;

	public final List<Label> generalComment = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
