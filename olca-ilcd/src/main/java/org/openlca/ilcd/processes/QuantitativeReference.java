package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.QuantitativeReferenceType;
import org.openlca.ilcd.commons.annotations.Label;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuantitativeReferenceType", propOrder = {
		"referenceFlows", "functionalUnit", "other" })
public class QuantitativeReference implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "referenceToReferenceFlow")
	public final List<Integer> referenceFlows = new ArrayList<>();

	@Label
	@XmlElement(name = "functionalUnitOrOther")
	public final List<LangString> functionalUnit = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAttribute(name = "type")
	public QuantitativeReferenceType type;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public QuantitativeReference clone() {
		QuantitativeReference clone = new QuantitativeReference();
		for (Integer ref : referenceFlows)
			clone.referenceFlows.add(ref);
		LangString.copy(functionalUnit, clone.functionalUnit);
		if (other != null)
			clone.other = other.clone();
		clone.type = type;
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}
}
