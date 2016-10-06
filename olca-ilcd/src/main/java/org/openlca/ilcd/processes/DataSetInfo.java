
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

import org.openlca.ilcd.commons.ClassificationInfo;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.annotations.FreeText;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataSetInformationType", propOrder = {
		"uuid",
		"name",
		"identifierOfSubDataSet",
		"synonyms",
		"complementingProcesses",
		"classificationInformation",
		"generalComment",
		"externalDocumentationSources",
		"other"
})
public class DataSetInfo implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "UUID", namespace = "http://lca.jrc.it/ILCD/Common", required = true)
	public String uuid;

	public ProcessName name;

	public String identifierOfSubDataSet;

	@FreeText
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<LangString> synonyms = new ArrayList<>();

	public ComplementingProcesseList complementingProcesses;

	public ClassificationInfo classificationInformation;

	@FreeText
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<LangString> generalComment = new ArrayList<>();

	@XmlElement(name = "referenceToExternalDocumentation")
	public final List<DataSetReference> externalDocumentationSources = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
