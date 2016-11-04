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

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.FlowCompleteness;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.annotations.FreeText;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompletenessType", propOrder = {
		"type",
		"supportedImpactMethods",
		"entries",
		"otherDetails",
		"other" })
public class Completeness implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "completenessProductModel")
	public FlowCompleteness type;

	@XmlElement(name = "referenceToSupportedImpactAssessmentMethods")
	public final List<Ref> supportedImpactMethods = new ArrayList<>();

	@XmlElement(name = "completenessElementaryFlows")
	public final List<FlowCompletenessEntry> entries = new ArrayList<>();

	@FreeText
	@XmlElement(name = "completenessOtherProblemField")
	public final List<LangString> otherDetails = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public Completeness clone() {
		Completeness clone = new Completeness();
		clone.type = type;
		Ref.copy(supportedImpactMethods, clone.supportedImpactMethods);
		for (FlowCompletenessEntry e : entries) {
			if (e == null)
				continue;
			clone.entries.add(e.clone());
		}
		LangString.copy(otherDetails, clone.otherDetails);
		if (other != null)
			clone.other = other.clone();
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}
}
