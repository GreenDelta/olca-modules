
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
import org.openlca.ilcd.commons.FreeText;
import org.openlca.ilcd.commons.Other;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TechnologyType", propOrder = {
		"technologyDescriptionAndIncludedProcesses",
		"referenceToIncludedProcesses",
		"technologicalApplicability",
		"referenceToTechnologyPictogramme",
		"referenceToTechnologyFlowDiagrammOrPicture",
		"other"
})
public class Technology implements Serializable {

	private final static long serialVersionUID = 1L;

	public final List<FreeText> technologyDescriptionAndIncludedProcesses = new ArrayList<>();

	public final List<DataSetReference> referenceToIncludedProcesses = new ArrayList<>();

	public final List<FreeText> technologicalApplicability = new ArrayList<>();

	public DataSetReference referenceToTechnologyPictogramme;

	public final List<DataSetReference> referenceToTechnologyFlowDiagrammOrPicture = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
