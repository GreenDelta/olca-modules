package org.openlca.ilcd.models;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.util.Models;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessDataSetType", propOrder = { "info" })
@XmlRootElement(name = "lifeCycleModelDataSet", namespace = "http://eplca.jrc.ec.europa.eu/ILCD/LifeCycleModel/2017")
public class Model implements IDataSet {

	@XmlElement(name = "lifeCycleModelInformation")
	public ModelInfo info;

	@XmlAttribute(name = "version")
	public String version;

	@XmlAttribute(name = "locations")
	public String locations;

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
		// TODO Auto-generated method stub
		return null;
	}
}
