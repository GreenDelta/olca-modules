package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.FreeText;
import org.openlca.ilcd.commons.Other;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MathematicalRelationsType", propOrder = { "modelDescription",
		"parameters", "other" })
public class ParameterSection implements Serializable {

	private final static long serialVersionUID = 1L;

	public final List<FreeText> modelDescription = new ArrayList<>();

	@XmlElement(name = "variableParameter")
	public final List<Parameter> parameters = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
