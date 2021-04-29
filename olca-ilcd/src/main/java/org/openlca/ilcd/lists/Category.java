package org.openlca.ilcd.lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CategoryType", propOrder = { "category" })
public class Category implements Serializable {

	private final static long serialVersionUID = 1L;

	public final List<Category> category = new ArrayList<>();

	@XmlAttribute(name = "id", required = true)
	public String id;

	@XmlAttribute(name = "name", required = true)
	public String name;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
