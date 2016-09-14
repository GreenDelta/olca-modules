
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

import org.openlca.ilcd.commons.Label;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.UncertaintyDistribution;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VariableParameterType", propOrder = {
		"formula",
		"meanValue",
		"minimumValue",
		"maximumValue",
		"uncertaintyDistributionType",
		"relativeStandardDeviation95In",
		"comment",
		"other"
})
public class Parameter implements Serializable {

	private final static long serialVersionUID = 1L;

	public String formula;

	public Double meanValue;

	public Double minimumValue;

	public Double maximumValue;

	public UncertaintyDistribution uncertaintyDistributionType;

	protected BigDecimal relativeStandardDeviation95In;

	public final List<Label> comment = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAttribute(name = "name", required = true)
	public String name;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
