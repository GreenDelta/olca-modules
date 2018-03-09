package org.openlca.ilcd.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.util.Models;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "info", "modelling", "adminInfo" })
@XmlRootElement(name = "lifeCycleModelDataSet", namespace = "http://eplca.jrc.ec.europa.eu/ILCD/LifeCycleModel/2017")
public class Model implements IDataSet {

	private static final long serialVersionUID = -5507252231374830139L;

	@XmlElement(name = "lifeCycleModelInformation")
	public ModelInfo info;

	@XmlElement(name = "modellingAndValidation")
	public Modelling modelling;

	@XmlElement(name = "administrativeInformation")
	public AdminInfo adminInfo;

	@XmlAttribute(name = "version")
	public String version;

	@XmlAttribute(name = "locations")
	public String locations;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public List<Classification> getClassifications() {
		return Models.getClassifications(this);
	}

	@Override
	public DataSetType getDataSetType() {
		return DataSetType.MODEL;
	}

	@Override
	public List<LangString> getName() {
		ModelName name = Models.getModelName(this);
		if (name == null)
			return Collections.emptyList();
		return name.name;
	}

	@Override
	public String getURI() {
		Publication pub = Models.getPublication(this);
		if (pub == null)
			return null;
		return pub.uri;
	}

	@Override
	public String getUUID() {
		DataSetInfo info = Models.getDataSetInfo(this);
		if (info == null)
			return null;
		return info.uuid;
	}

	@Override
	public String getVersion() {
		Publication pub = Models.getPublication(this);
		if (pub == null)
			return null;
		return pub.version;
	}
}
