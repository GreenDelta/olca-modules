package org.openlca.ilcd.units;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UnitType", propOrder = {
		"name",
		"factor",
		"comment",
		"other"
})
public class Unit implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(required = true)
	public String name;

	@XmlElement(name = "meanValue")
	public double factor;

	@XmlElement(name = "generalComment")
	public final List<LangString> comment = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAttribute(name = "dataSetInternalID")
	public int id;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public Unit clone() {
		Unit clone = new Unit();
		clone.name = name;
		clone.factor = factor;
		LangString.copy(comment, clone.comment);
		if (other != null)
			clone.other = other.clone();
		clone.id = id;
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}
}
