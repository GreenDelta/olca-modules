package org.openlca.ilcd.contacts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.annotations.Label;
import org.openlca.ilcd.commons.annotations.ShortText;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataSetInformationType", propOrder = { "uuid", "shortName",
		"name", "classifications", "contactAddress", "telephone",
		"telefax", "email", "wwwAddress", "centralContactPoint", "description",
		"belongsTo", "logo", "other" })
public class DataSetInfo implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "UUID", namespace = "http://lca.jrc.it/ILCD/Common", required = true)
	public String uuid;

	@Label
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<LangString> shortName = new ArrayList<>();

	@Label
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<LangString> name = new ArrayList<>();

	@XmlElementWrapper(name = "classificationInformation")
	@XmlElement(name = "classification", namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<Classification> classifications = new ArrayList<>();

	@ShortText
	public final List<LangString> contactAddress = new ArrayList<>();

	public String telephone;

	public String telefax;

	public String email;

	@XmlElement(name = "WWWAddress")
	public String wwwAddress;

	@ShortText
	public final List<LangString> centralContactPoint = new ArrayList<>();

	@ShortText
	@XmlElement(name = "contactDescriptionOrComment")
	public final List<LangString> description = new ArrayList<>();

	@XmlElement(name = "referenceToContact")
	public final List<Ref> belongsTo = new ArrayList<>();

	@XmlElement(name = "referenceToLogo")
	public Ref logo;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
