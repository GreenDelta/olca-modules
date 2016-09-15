package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.ArrayList;
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

import org.openlca.ilcd.commons.Label;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.QuantitativeReferenceType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuantitativeReferenceType", propOrder = {
		"referenceToReferenceFlow", "functionalUnitOrOther", "other" })
public class QuantitativeReference implements Serializable {

	private final static long serialVersionUID = 1L;

	public final List<Integer> referenceToReferenceFlow = new ArrayList<>();

	public final List<Label> functionalUnitOrOther = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAttribute(name = "type")
	public QuantitativeReferenceType type;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
