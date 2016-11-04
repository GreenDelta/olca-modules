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
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.ModellingApproach;
import org.openlca.ilcd.commons.ModellingPrinciple;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.ProcessType;
import org.openlca.ilcd.commons.annotations.FreeText;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LCIMethodAndAllocationType", propOrder = {
		"processType",
		"principle",
		"principleComment",
		"approaches",
		"approachComment",
		"constants",
		"constantsComment",
		"methodSources",
		"other" })
public class Method implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "typeOfDataSet")
	public ProcessType processType;

	@XmlElement(name = "LCIMethodPrinciple")
	public ModellingPrinciple principle;

	@FreeText
	@XmlElement(name = "deviationsFromLCIMethodPrinciple")
	public final List<LangString> principleComment = new ArrayList<>();

	@XmlElement(name = "LCIMethodApproaches")
	public final List<ModellingApproach> approaches = new ArrayList<>();

	@FreeText
	@XmlElement(name = "deviationsFromLCIMethodApproaches")
	public final List<LangString> approachComment = new ArrayList<>();

	@FreeText
	@XmlElement(name = "modellingConstants")
	public final List<LangString> constants = new ArrayList<>();

	@FreeText
	@XmlElement(name = "deviationsFromModellingConstants")
	public final List<LangString> constantsComment = new ArrayList<>();

	@XmlElement(name = "referenceToLCAMethodDetails")
	public final List<Ref> methodSources = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public Method clone() {
		Method clone = new Method();
		clone.processType = processType;
		clone.principle = principle;
		LangString.copy(principleComment, clone.principleComment);
		for (ModellingApproach a : approaches)
			clone.approaches.add(a);
		LangString.copy(approachComment, clone.approachComment);
		LangString.copy(constants, clone.constants);
		LangString.copy(constantsComment, clone.constantsComment);
		Ref.copy(methodSources, clone.methodSources);
		if (other != null)
			clone.other = other.clone();
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}
}
