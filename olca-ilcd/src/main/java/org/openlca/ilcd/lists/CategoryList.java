package org.openlca.ilcd.lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CategoriesType", propOrder = { "categories" })
public class CategoryList implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "category", required = true)
	public final List<Category> categories = new ArrayList<>();

	@XmlAttribute(name = "dataType", required = true)
	public ContentType type;

}
