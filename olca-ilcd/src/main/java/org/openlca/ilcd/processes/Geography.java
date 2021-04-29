package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Other;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GeographyType", propOrder = { "location", "subLocations",
		"other" })
public class Geography implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "locationOfOperationSupplyOrProduction")
	public Location location;

	@XmlElement(name = "subLocationOfOperationSupplyOrProduction")
	public final List<SubLocation> subLocations = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public Geography clone() {
		Geography clone = new Geography();
		if (location != null)
			clone.location = location.clone();
		for (SubLocation sub : subLocations)
			clone.subLocations.add(sub.clone());
		if (other != null)
			clone.other = other.clone();
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}
}
