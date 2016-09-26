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
@XmlType(name = "FlowCategorizationType", propOrder = { "categories", "other" })
public class FlowCategorization implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "category", required = true)
	public final List<Category> categories = new ArrayList<>();

	public Other other;

	@XmlAttribute(name = "name")
	public String name;

	@XmlAttribute(name = "categories")
	@XmlSchemaType(name = "anyURI")
	public String url;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
