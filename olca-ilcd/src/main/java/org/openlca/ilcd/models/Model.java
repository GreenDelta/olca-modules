package org.openlca.ilcd.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessDataSetType", propOrder = { "info" })
public class Model {

	@XmlElement(name = "lifeCycleModelInformation")
	public ModelInfo info;
}
