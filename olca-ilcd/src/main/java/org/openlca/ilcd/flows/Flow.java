
package org.openlca.ilcd.flows;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FlowDataSetType", propOrder = {
		"flowInfo",
		"modelling",
		"adminInfo",
		"flowPropertyList",
		"other"
})
public class Flow implements IDataSet {

	private final static long serialVersionUID = 1L;

	@XmlElement(required = true, name = "flowInformation")
	public FlowInfo flowInfo;

	@XmlElement(name = "modellingAndValidation")
	public Modelling modelling;

	@XmlElement(name = "administrativeInformation")
	public AdminInfo adminInfo;

	@XmlElement(name = "flowProperties")
	public FlowPropertyList flowPropertyList;

	@XmlAttribute(name = "version", required = true)
	public String version;

	@XmlAttribute(name = "locations")
	public String locations;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public DataSetType getDataSetType() {
		return DataSetType.FLOW;
	}

	@Override
	public String getURI() {
		if (adminInfo == null || adminInfo.publication == null)
			return null;
		return adminInfo.publication.uri;
	}

	@Override
	public String getUUID() {
		if (flowInfo == null || flowInfo.dataSetInfo == null)
			return null;
		return flowInfo.dataSetInfo.uuid;
	}

	@Override
	public String getVersion() {
		if (adminInfo == null || adminInfo.publication == null)
			return null;
		return adminInfo.publication.version;
	}

	@Override
	public List<Classification> getClassifications() {
		if (flowInfo == null || flowInfo.dataSetInfo == null)
			return Collections.emptyList();
		FlowCategoryInfo info = flowInfo.dataSetInfo.classificationInformation;
		if (info == null)
			return Collections.emptyList();
		return info.classifications;
	}

	@Override
	public List<LangString> getName() {
		if (flowInfo == null || flowInfo.dataSetInfo == null)
			return Collections.emptyList();
		FlowName name = flowInfo.dataSetInfo.name;
		if (name == null)
			return Collections.emptyList();
		return name.baseName;
	}
}
