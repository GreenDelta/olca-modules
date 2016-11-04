package org.openlca.ilcd.lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.Ref;

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
