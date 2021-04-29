package org.openlca.ilcd.lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.Ref;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CategorySystemType", propOrder = { "source", "categories" })
public class CategorySystem implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "referenceToSource")
	public Ref source;

	@XmlElement(required = true)
	public final List<CategoryList> categories = new ArrayList<>();

	@XmlAttribute(name = "name")
	public String name;

}
