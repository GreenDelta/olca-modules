
package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.annotations.FreeText;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LocationOfOperationSupplyOrProductionType", propOrder = {
		"description",
		"other"
})
public class Location implements Serializable {

	private final static long serialVersionUID = 1L;

	/**
	 * Further explanations about additional aspects of the location: e.g. a
	 * company and/or site description and address, whether for certain
	 * sub-areas within the "Location" the data set is not valid, whether data
	 * is only valid for certain regions within the location indicated, or
	 * whether certain elementary flows or intermediate product flows are
	 * extrapolated from another geographical area.
	 */
	@FreeText
	@XmlElement(name = "descriptionOfRestrictions")
	public final List<LangString> description = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	/**
	 * Location, country or region the data set represents. [Note 1: This field
	 * does not refer to e.g. the country in which a specific site is located
	 * that is represented by this data set but to the actually represented
	 * country, region, or site. Note 2: Entry can be of type "two-letter ISO
	 * 3166 country code" for countries, "seven-letter regional codes" for
	 * regions or continents, or "market areas and market organisations", as
	 * predefined for the ILCD. Also a name for e.g. a specific plant etc. can
	 * be given here (e.g. "FR, Lyon, XY Company, Z Site"; user defined). Note
	 * 3: The fact whether the entry refers to production or to consumption /
	 * supply has to be stated in the name-field "Mix and location types" e.g.
	 * as "Production mix".]
	 */
	@XmlAttribute(name = "location")
	public String code;

	/**
	 * Geographical latitude and longitude reference of "Location" /
	 * "Sub-location". For area-type locations (e.g. countries, continents) the
	 * field is empty.
	 */
	@XmlAttribute(name = "latitudeAndLongitude")
	public String latitudeAndLongitude;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public Location clone() {
		Location clone = new Location();
		LangString.copy(description, clone.description);
		if (other != null)
			clone.other = other.clone();
		clone.code = code;
		clone.latitudeAndLongitude = latitudeAndLongitude;
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}
}
