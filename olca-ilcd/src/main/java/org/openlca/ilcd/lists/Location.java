
package org.openlca.ilcd.lists;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LocationType", namespace = "http://lca.jrc.it/ILCD/Locations", propOrder = {
		"name"
})
public class Location implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlValue
	public String name;

	@XmlAttribute(name = "value", required = true)
	public String code;

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Location))
			return false;
		Location other = (Location) obj;
		return Objects.equals(this.code, other.code)
				&& Objects.equals(this.name, other.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, name);
	}

	@Override
	public Location clone() {
		Location clone = new Location();
		clone.code = code;
		clone.name = name;
		return clone;
	}
}
