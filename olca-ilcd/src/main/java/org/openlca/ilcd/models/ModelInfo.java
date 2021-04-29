package org.openlca.ilcd.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "dataSetInfo", "quantitativeReference", "technology" })
public class ModelInfo {

	@XmlElement(name = "dataSetInformation")
	public DataSetInfo dataSetInfo;

	@XmlElement(name = "quantitativeReference")
	public QuantitativeReference quantitativeReference;

	@XmlElement(name = "technology")
	public Technology technology;

}
