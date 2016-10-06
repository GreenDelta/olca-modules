package org.openlca.ilcd.sources;

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
import org.openlca.ilcd.commons.PublicationType;
import org.openlca.ilcd.commons.annotations.FreeText;
import org.openlca.ilcd.commons.annotations.Label;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataSetInformationType", propOrder = { "uuid", "shortName",
		"classificationInformation", "sourceCitation", "publicationType",
		"sourceDescriptionOrComment", "referenceToDigitalFile",
		"referenceToContact", "referenceToLogo", "other" })
public class DataSetInfo implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "UUID", namespace = "http://lca.jrc.it/ILCD/Common", required = true)
	public String uuid;

	@Label
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<LangString> shortName = new ArrayList<>();

	public ClassificationInfo classificationInformation;

	public String sourceCitation;

	public PublicationType publicationType;

	@FreeText
	public final List<LangString> sourceDescriptionOrComment = new ArrayList<>();

	public final List<DigitalFileRef> referenceToDigitalFile = new ArrayList<>();

	public final List<DataSetReference> referenceToContact = new ArrayList<>();

	public DataSetReference referenceToLogo;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public Map<QName, String> otherAttributes = new HashMap<>();

}
