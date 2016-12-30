
package org.openlca.ilcd.flowproperties;

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
@XmlType(name = "FlowPropertyDataSetType", propOrder = {
		"flowPropertyInfo",
		"modelling",
		"adminInfo",
		"other"
})
public class FlowProperty implements IDataSet {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "flowPropertiesInformation", required = true)
	public FlowPropertyInfo flowPropertyInfo;

	@XmlElement(name = "modellingAndValidation")
	public Modelling modelling;

	@XmlElement(name = "administrativeInformation")
	public AdminInfo adminInfo;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAttribute(name = "version", required = true)
	public String version;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public DataSetType getDataSetType() {
		return DataSetType.FLOW_PROPERTY;
	}

	@Override
	public String getURI() {
		if (adminInfo == null || adminInfo.publication == null)
			return null;
		return adminInfo.publication.uri;
	}

	@Override
	public String getUUID() {
		if (flowPropertyInfo == null || flowPropertyInfo.dataSetInfo == null)
			return null;
		return flowPropertyInfo.dataSetInfo.uuid;
	}

	@Override
	public String getVersion() {
		if (adminInfo == null || adminInfo.publication == null)
			return null;
		return adminInfo.publication.version;
	}

	@Override
	public List<Classification> getClassifications() {
		if (flowPropertyInfo == null || flowPropertyInfo.dataSetInfo == null)
			return Collections.emptyList();
		return flowPropertyInfo.dataSetInfo.classifications;
	}

	@Override
	public List<LangString> getName() {
		if (flowPropertyInfo == null || flowPropertyInfo.dataSetInfo == null)
			return Collections.emptyList();
		return flowPropertyInfo.dataSetInfo.name;
	}

}
