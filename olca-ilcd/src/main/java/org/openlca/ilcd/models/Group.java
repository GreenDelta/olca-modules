package org.openlca.ilcd.models;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.LangString;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "name" })
public class Group {

	@XmlAttribute(name = "id")
	public int id;

	@XmlElement(name = "groupName")
	public List<LangString> name = new ArrayList<>();

}
