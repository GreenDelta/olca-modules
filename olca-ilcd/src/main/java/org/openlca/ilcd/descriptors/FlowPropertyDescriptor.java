
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
		"unitGroup"
})
public class FlowPropertyDescriptor extends Descriptor {

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public final List<LangString> synonyms = new ArrayList<>();

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/FlowProperty")
	public UnitGroupReference unitGroup;

	@XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
	@XmlSchemaType(name = "anyURI")
	public String href;

	@XmlAttribute(name = "sourceId", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public String sourceId;

	@Override
	protected DataSetType getType() {
		return DataSetType.FLOW_PROPERTY;
	}
}
