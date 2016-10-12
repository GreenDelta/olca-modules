
package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Other;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModellingAndValidationType", propOrder = {
		"lciMethod",
		"representativeness",
		"completeness",
		"validation",
		"complianceDeclarations",
		"other"
})
public class Modelling implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "LCIMethodAndAllocation")
	public LCIMethod lciMethod;

	@XmlElement(name = "dataSourcesTreatmentAndRepresentativeness")
	public Representativeness representativeness;

	public Completeness completeness;

	public Validation validation;

	public ComplianceDeclarationList complianceDeclarations;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
