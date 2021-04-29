package org.openlca.ilcd.lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CategoriesType", propOrder = { "categories" })
public class CategoryList implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "category", required = true)
	public final List<Category> categories = new ArrayList<>();

	@XmlAttribute(name = "dataType", required = true)
	public ContentType type;

}
