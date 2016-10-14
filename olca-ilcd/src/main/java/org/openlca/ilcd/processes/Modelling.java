
package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Other;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModellingAndValidationType", propOrder = {
		"method",
		"representativeness",
		"completeness",
		"validation",
		"complianceDeclatations",
		"other"
})
public class Modelling implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "LCIMethodAndAllocation")
	public Method method;

	@XmlElement(name = "dataSourcesTreatmentAndRepresentativeness")
	public Representativeness representativeness;

	public Completeness completeness;

	public Validation validation;

	@XmlElementWrapper(name = "complianceDeclarations")
	@XmlElement(name = "compliance", required = true)
	public ComplianceDeclaration[] complianceDeclatations;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public Modelling clone() {
		Modelling clone = new Modelling();
		if (method != null)
			clone.method = method.clone();
		if (representativeness != null)
			clone.representativeness = representativeness.clone();
		if (completeness != null)
			clone.completeness = completeness.clone();
		if (validation != null)
			clone.validation = validation.clone();
		if (complianceDeclatations != null) {
			clone.complianceDeclatations = new ComplianceDeclaration[complianceDeclatations.length];
			for (int i = 0; i < complianceDeclatations.length; i++) {
				ComplianceDeclaration d = complianceDeclatations[i];
				if (d == null)
					continue;
				clone.complianceDeclatations[i] = d.clone();
			}
		}
		if (other != null)
			clone.other = other.clone();
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}

}
