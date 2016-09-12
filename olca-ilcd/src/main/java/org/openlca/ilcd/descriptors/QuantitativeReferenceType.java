package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuantitativeReferenceType", propOrder = { "referenceFlow",
		"functionalUnit" })
public class QuantitativeReferenceType implements Serializable {

	private final static long serialVersionUID = 1L;

	public final List<ReferenceFlowType> referenceFlow = new ArrayList<>();

	public final List<LangString> functionalUnit = new ArrayList<>();

	@XmlAttribute(name = "type")
	public TypeOfQuantitativeReferenceValues type;

}
