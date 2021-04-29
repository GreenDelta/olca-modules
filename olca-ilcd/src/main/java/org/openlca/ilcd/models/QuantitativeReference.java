package org.openlca.ilcd.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "refProcess" })
public class QuantitativeReference {

	@XmlElement(name = "referenceToReferenceProcess")
	public Integer refProcess;
}