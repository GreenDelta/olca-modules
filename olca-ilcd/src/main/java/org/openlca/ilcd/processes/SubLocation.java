package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.annotations.FreeText;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubLocationOfOperationSupplyOrProductionType", propOrder = {
		"description",
		"other"
})
public class SubLocation implements Serializable {

	private final static long serialVersionUID = 1L;

	@FreeText
	@XmlElement(name = "descriptionOfRestrictions")
	public final List<LangString> description = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAttribute(name = "subLocation")
	public String subLocation;

	@XmlAttribute(name = "latitudeAndLongitude")
	public String latitudeAndLongitude;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public SubLocation clone() {
		SubLocation clone = new SubLocation();
		LangString.copy(description, clone.description);
		if (other != null)
			clone.other = other.clone();
		clone.subLocation = subLocation;
		clone.latitudeAndLongitude = latitudeAndLongitude;
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}

}
