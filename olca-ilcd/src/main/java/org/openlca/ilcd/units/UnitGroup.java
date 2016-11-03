
package org.openlca.ilcd.units;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.AdminInfo;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Other;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UnitGroupDataSetType", propOrder = {
		"unitGroupInfo",
		"modelling",
		"adminInfo",
		"units",
		"other"
})
public class UnitGroup implements IDataSet {

	private final static long serialVersionUID = 1L;

	@XmlElement(required = true, name = "unitGroupInformation")
	public UnitGroupInfo unitGroupInfo;

	@XmlElement(name = "modellingAndValidation")
	public ModellingAndValidation modelling;

	@XmlElement(name = "administrativeInformation")
	public AdminInfo adminInfo;

	public UnitList units;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAttribute(name = "version", required = true)
	public String version;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public DataSetType getDataSetType() {
		return DataSetType.UNIT_GROUP;
	}

	@Override
	public String getURI() {
		if (adminInfo == null || adminInfo.publication == null)
			return null;
		return adminInfo.publication.uri;
	}

	@Override
	public String getUUID() {
		if (unitGroupInfo == null || unitGroupInfo.dataSetInfo == null)
			return null;
		return unitGroupInfo.dataSetInfo.uuid;
	}

	@Override
	public String getVersion() {
		if (adminInfo == null || adminInfo.publication == null)
			return null;
		return adminInfo.publication.version;
	}
}
