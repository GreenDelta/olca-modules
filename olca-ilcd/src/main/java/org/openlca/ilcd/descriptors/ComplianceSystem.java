
package org.openlca.ilcd.descriptors;

import java.io.Serializable;

import org.openlca.ilcd.commons.Compliance;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

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

	public Compliance overallCompliance;

	public Compliance nomenclatureCompliance;

	public Compliance methodologicalCompliance;

	public Compliance reviewCompliance;

	public Compliance documentationCompliance;

	public Compliance qualityCompliance;

	@XmlAttribute(name = "name")
	public String name;

}
