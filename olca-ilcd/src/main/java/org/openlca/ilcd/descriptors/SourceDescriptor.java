package org.openlca.ilcd.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.sources.SourceType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"uuid",
		"uri",
		"version",
		"name",
		"shortName",
		"classification",
		"comment",
		"citation",
		"publicationType",
		"file",
		"belongsTo" })
public class SourceDescriptor extends Descriptor {

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public String shortName;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Source")
	public LangString citation;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Source")
	public SourceType publicationType;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Source")
	public final List<DataSetReference> file = new ArrayList<>();

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Source")
	public DataSetReference belongsTo;

	@XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
	@XmlSchemaType(name = "anyURI")
	public String href;

	@XmlAttribute(name = "sourceId", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public String sourceId;

	@Override
	protected DataSetType getType() {
		return DataSetType.SOURCE;
	}
}
