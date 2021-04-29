package org.openlca.ilcd.flows;

import java.io.Serializable;
import java.util.List;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.annotations.FreeText;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TechnologyType", propOrder = {
		"technologicalApplicability",
		"technicalSpecifications",
		"other"
})
public class Technology implements Serializable {

	private final static long serialVersionUID = 1L;

	@FreeText
	public List<LangString> technologicalApplicability;

	@XmlElement(name = "referenceToTechnicalSpecification")
	public List<Ref> technicalSpecifications;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

}
