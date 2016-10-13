
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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.annotations.FreeText;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataSetInformationType", propOrder = {
		"uuid",
		"name",
		"subIdentifier",
		"synonyms",
		"complementingProcesses",
		"classifications",
		"comment",
		"externalDocs",
		"other"
})
public class DataSetInfo implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "UUID", namespace = "http://lca.jrc.it/ILCD/Common", required = true)
	public String uuid;

	public ProcessName name;

	/**
	 * Identifier of a sub-set of a complete process data set. This can be the
	 * life cycle stage that a data set covers (such as used in EPDs for modular
	 * LCI reporting, with the inventory split up into "resource extraction
	 * stage", "production stage", "use stage" and "end-of-life stage"). Or it
	 * can be e.g. the type of emission source from which the elementary flows
	 * of the Inputs and Outputs stems (e.g. "incineration-related",
	 * "transport-related", etc.). Together with the field "Complementing
	 * processes" this allows to split up a process data set into a number of
	 * clearly identified data sets, each carrying only a part of the inventory
	 * and that together represent the complete inventory. Care has to be taken
	 * when naming the reference flow, to avoid misinterpretation.
	 */
	@XmlElement(name = "identifierOfSubDataSet")
	public String subIdentifier;

	@FreeText
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<LangString> synonyms = new ArrayList<>();

	@XmlElementWrapper(name = "complementingProcesses")
	@XmlElement(name = "referenceToComplementingProcess", required = true)
	public DataSetReference[] complementingProcesses;

	@XmlElementWrapper(name = "classificationInformation")
	@XmlElement(name = "classification", namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<Classification> classifications = new ArrayList<>();

	@FreeText
	@XmlElement(name = "generalComment", namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<LangString> comment = new ArrayList<>();

	@XmlElement(name = "referenceToExternalDocumentation")
	public final List<DataSetReference> externalDocs = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
