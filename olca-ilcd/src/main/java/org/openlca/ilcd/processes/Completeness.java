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

import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.FlowCompleteness;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.annotations.FreeText;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompletenessType", propOrder = { "completenessProductModel",
		"supportedLciaMethods", "completenessElementaryFlows",
		"completenessOtherProblemField", "other" })
public class Completeness implements Serializable {

	private final static long serialVersionUID = 1L;

	public FlowCompleteness completenessProductModel;

	@XmlElement(name = "referenceToSupportedImpactAssessmentMethods")
	public final List<DataSetReference> supportedLciaMethods = new ArrayList<>();

	public final List<ElementaryFlowCompleteness> completenessElementaryFlows = new ArrayList<>();

	@FreeText
	public final List<LangString> completenessOtherProblemField = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
