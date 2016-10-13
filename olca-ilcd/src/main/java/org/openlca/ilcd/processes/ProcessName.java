
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
		"name",
		"technicalDetails",
		"mixAndLocation",
		"flowProperties",
		"other"
})
public class ProcessName implements Serializable {

	private final static long serialVersionUID = 1L;

	/**
	 * General descriptive name of the process and/or its main good(s) or
	 * service(s) and/or it's level of processing.
	 */
	@Label
	@XmlElement(name = "baseName", required = true)
	public final List<LangString> name = new ArrayList<>();

	/**
	 * Specifying information on the good, service, or process in technical
	 * term(s): treatment received, standard fulfilled, product quality, use
	 * information, production route name, educt name, primary / secondary etc.
	 * Separated by commata.
	 */
	@Label
	@XmlElement(name = "treatmentStandardsRoutes")
	public final List<LangString> technicalDetails = new ArrayList<>();

	/**
	 * Specifying information on the good, service, or process whether being a
	 * production mix or consumption mix, location type of availability (such as
	 * e.g. "to consumer" or "at plant"). Separated by commata.
	 */
	@Label
	@XmlElement(name = "mixAndLocationTypes")
	public final List<LangString> mixAndLocation = new ArrayList<>();

	/**
	 * Further, quantitative specifying information on the good, service or
	 * process in technical term(s): qualifying constituent(s)-content and / or
	 * energy-content per unit etc. as appropriate. Separated by commata. (Note:
	 * non-qualifying flow properties, CAS No, Synonyms, Chemical formulas etc.
	 * are documented exclusively in the "Flow data set".)
	 */
	@Label
	@XmlElement(name = "functionalUnitFlowProperties")
	public final List<LangString> flowProperties = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public ProcessName clone() {
		ProcessName clone = new ProcessName();
		LangString.copy(name, clone.name);
		LangString.copy(technicalDetails, clone.technicalDetails);
		LangString.copy(mixAndLocation, clone.mixAndLocation);
		LangString.copy(flowProperties, clone.flowProperties);
		if (other != null)
			clone.other = other.clone();
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}
}
