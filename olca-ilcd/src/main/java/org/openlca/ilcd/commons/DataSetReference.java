package org.openlca.ilcd.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.annotations.ShortText;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalReferenceType", propOrder = { "subReference",
		"description", "other" })
public class DataSetReference implements Serializable {

	private final static long serialVersionUID = 1L;

	public final List<String> subReference = new ArrayList<>();

	@ShortText
	@XmlElement(name = "shortDescription")
	public final List<LangString> description = new ArrayList<>();

	public Other other;

	@XmlAttribute(name = "type", required = true)
	public DataSetType type;

	@XmlAttribute(name = "refObjectId")
	public String uuid;

	@XmlAttribute(name = "version")
	public String version;

	@XmlAttribute(name = "uri")
	@XmlSchemaType(name = "anyURI")
	public String uri;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public String toString() {
		return "DataSetReference [type=" + type + ", uuid=" + uuid + "]";
	}

}
