package org.openlca.ilcd.processes;

import java.io.Serializable;
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

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.UncertaintyDistribution;
import org.openlca.ilcd.commons.annotations.Label;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LCIAResultType", propOrder = {
		"method",
		"amount",
		"uncertaintyDistribution",
		"relativeStandardDeviation95In",
		"comment",
		"other"
})
public class LCIAResult implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "referenceToLCIAMethodDataSet", required = true)
	public Ref method;

	@XmlElement(name = "meanAmount")
	public double amount;

	@XmlElement(name = "uncertaintyDistributionType")
	public UncertaintyDistribution uncertaintyDistribution;

	public Double relativeStandardDeviation95In;

	@Label
	@XmlElement(name = "generalComment")
	public final List<LangString> comment = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public LCIAResult clone() {
		LCIAResult clone = new LCIAResult();
		if (method != null)
			clone.method = method.clone();
		clone.amount = amount;
		clone.uncertaintyDistribution = uncertaintyDistribution;
		clone.relativeStandardDeviation95In = relativeStandardDeviation95In;
		LangString.copy(comment, clone.comment);
		if (other != null)
			clone.other = other.clone();
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}
}
