
package org.openlca.ilcd.flows;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.annotations.FreeText;

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
