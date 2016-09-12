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
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.Label;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.ShortText;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataSetInformationType", propOrder = { "uuid", "shortName",
		"name", "classificationInformation", "contactAddress", "telephone",
		"telefax", "email", "wwwAddress", "centralContactPoint", "description",
		"belongsTo", "logo", "other" })
public class DataSetInfo implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "UUID", namespace = "http://lca.jrc.it/ILCD/Common", required = true)
	public String uuid;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<Label> shortName = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<Label> name = new ArrayList<>();

	public ClassificationInformation classificationInformation;

	public final List<ShortText> contactAddress = new ArrayList<>();

	public String telephone;

	public String telefax;

	public String email;

	@XmlElement(name = "WWWAddress")
	public String wwwAddress;

	public final List<ShortText> centralContactPoint = new ArrayList<>();

	@XmlElement(name = "contactDescriptionOrComment")
	public final List<ShortText> description = new ArrayList<>();

	@XmlElement(name = "referenceToContact")
	public final List<DataSetReference> belongsTo = new ArrayList<>();

	@XmlElement(name = "referenceToLogo")
	public DataSetReference logo;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
