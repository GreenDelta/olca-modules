
package org.openlca.ilcd.descriptors;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.commons.LangString;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"uuid",
		"uri",
		"version",
		"name",
		"classification",
		"comment",
		"synonyms",
		"flowCategorization",
		"type",
		"casNumber",
		"sumFormula",
		"referenceFlowProperty"
})
public class FlowDescriptor extends Descriptor {

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public final List<LangString> synonyms = new ArrayList<>();

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Flow")
	public final List<FlowCategorization> flowCategorization = new ArrayList<>();

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Flow")
	public FlowType type;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Flow")
	public String casNumber;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Flow")
	public String sumFormula;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Flow")
	public ReferenceFlowProperty referenceFlowProperty;

	@XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
	@XmlSchemaType(name = "anyURI")
	public String href;

	@XmlAttribute(name = "sourceId", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public String sourceId;

	@Override
	protected DataSetType getType() {
		return DataSetType.FLOW;
	}

}
