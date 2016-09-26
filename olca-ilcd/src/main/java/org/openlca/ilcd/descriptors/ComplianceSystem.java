
package org.openlca.ilcd.descriptors;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"reference",
		"overallCompliance",
		"nomenclatureCompliance",
		"methodologicalCompliance",
		"reviewCompliance",
		"documentationCompliance",
		"qualityCompliance"
})
@XmlRootElement(name = "complianceSystem")
public class ComplianceSystem implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public DataSetReference reference;

	public ComplianceValues overallCompliance;

	public ComplianceValues nomenclatureCompliance;

	public ComplianceValues methodologicalCompliance;

	public ComplianceValues reviewCompliance;

	public ComplianceValues documentationCompliance;

	public ComplianceValues qualityCompliance;

	@XmlAttribute(name = "name")
	public String name;

}
