
package org.openlca.ilcd.descriptors;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.Compliance;

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
