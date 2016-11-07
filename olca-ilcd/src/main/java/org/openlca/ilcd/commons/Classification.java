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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClassificationType", propOrder = { "categories", "other" })
public class Classification implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "class", required = true)
	public final List<Category> categories = new ArrayList<>();

	public Other other;

	@XmlAttribute(name = "name")
	public String name;

	@XmlAttribute(name = "classes")
	@XmlSchemaType(name = "anyURI")
	public String url;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public Classification clone() {
		Classification clone = new Classification();
		for (Category c : categories)
			clone.categories.add(c.clone());
		if (other != null)
			clone.other = other.clone();
		clone.name = name;
		clone.url = url;
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}
}
