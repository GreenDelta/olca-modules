package org.openlca.ilcd.models;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.annotations.FreeText;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataSetInformationType", propOrder = {
		"uuid",
		"name",
		"classifications",
		"comment",
		"externalDocs"
})
public class DataSetInfo {

	@XmlElement(name = "UUID", namespace = "http://lca.jrc.it/ILCD/Common")
	public String uuid;

	@XmlElement(name = "name")
	public ModelName name;

	@XmlElement(name = "classificationInformation")
	public ClassificationList classifications;

	@FreeText
	@XmlElement(name = "generalComment", namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<LangString> comment = new ArrayList<>();

	@XmlElement(name = "referenceToExternalDocumentation")
	public final List<Ref> externalDocs = new ArrayList<>();
}
