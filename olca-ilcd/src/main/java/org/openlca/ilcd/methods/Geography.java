package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.annotations.FreeText;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GeographyType", propOrder = {
		"interventionLocation",
		"interventionSubLocations",
		"impactLocation",
		"description",
		"other" })
public class Geography implements Serializable {

	private final static long serialVersionUID = 1L;

	public Location interventionLocation;

	@XmlElement(name = "interventionSubLocation")
	public final List<Location> interventionSubLocations = new ArrayList<>();

	public Location impactLocation;

	@FreeText
	@XmlElement(name = "geographicalRepresentativenessDescription")
	public final List<LangString> description = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
