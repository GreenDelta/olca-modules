
package org.openlca.ilcd.flowproperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Compliance;
import org.openlca.ilcd.commons.Ref;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComplianceType", propOrder = {
		"referenceToComplianceSystem",
		"approvalOfOverallCompliance"
})
public class ComplianceDeclaration implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", required = true)
	public Ref referenceToComplianceSystem;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Compliance approvalOfOverallCompliance;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
