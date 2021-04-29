package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.annotations.FreeText;
import org.openlca.ilcd.commons.annotations.ShortText;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ImpactModelType", propOrder = {
		"name",
		"description",
		"sources",
		"includedMethods",
		"consideredMechanisms",
		"flowCharts",
		"other"
})
public class ImpactModel implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "modelName")
	public String name;

	@FreeText
	@XmlElement(name = "modelDescription")
	public final List<LangString> description = new ArrayList<>();

	@XmlElement(name = "referenceToModelSource")
	public final List<Ref> sources = new ArrayList<>();

	@XmlElement(name = "referenceToIncludedMethods")
	public final List<Ref> includedMethods = new ArrayList<>();

	@ShortText
	public final List<LangString> consideredMechanisms = new ArrayList<>();

	@XmlElement(name = "referenceToMethodologyFlowChart")
	public final List<Ref> flowCharts = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
