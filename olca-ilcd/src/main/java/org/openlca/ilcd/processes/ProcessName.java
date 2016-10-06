
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

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.annotations.Label;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NameType", propOrder = {
		"baseName",
		"treatmentStandardsRoutes",
		"mixAndLocationTypes",
		"functionalUnitFlowProperties",
		"other"
})
public class ProcessName implements Serializable {

	private final static long serialVersionUID = 1L;

	@Label
	@XmlElement(required = true)
	public final List<LangString> baseName = new ArrayList<>();

	@Label
	public final List<LangString> treatmentStandardsRoutes = new ArrayList<>();

	@Label
	public final List<LangString> mixAndLocationTypes = new ArrayList<>();

	@Label
	public final List<LangString> functionalUnitFlowProperties = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
