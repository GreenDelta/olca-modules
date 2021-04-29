package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.annotations.FreeText;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MathematicalRelationsType", propOrder = { "description",
		"parameters", "other" })
public class ParameterSection implements Serializable {

	private final static long serialVersionUID = 1L;

	@FreeText
	@XmlElement(name = "modelDescription")
	public final List<LangString> description = new ArrayList<>();

	@XmlElement(name = "variableParameter")
	public final List<Parameter> parameters = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public ParameterSection clone() {
		ParameterSection clone = new ParameterSection();
		LangString.copy(description, clone.description);
		for (Parameter p : parameters) {
			if (p == null)
				continue;
			clone.parameters.add(p.clone());
		}
		if (other != null)
			clone.other = other.clone();
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}
}
