package org.openlca.ilcd.methods;

import java.io.Serializable;

import org.openlca.ilcd.commons.Compliance;
import org.openlca.ilcd.commons.Ref;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComplianceType", propOrder = {
		"complianceSystem",
		"approvalOfOverallCompliance",
		"nomenclatureCompliance",
		"methodologicalCompliance",
		"reviewCompliance",
		"documentationCompliance",
		"qualityCompliance"
})
public class ComplianceDeclaration implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "referenceToComplianceSystem", namespace = "http://lca.jrc.it/ILCD/Common", required = true)
	public Ref complianceSystem;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Compliance approvalOfOverallCompliance;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Compliance nomenclatureCompliance;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Compliance methodologicalCompliance;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Compliance reviewCompliance;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Compliance documentationCompliance;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Compliance qualityCompliance;

}
