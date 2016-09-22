
package org.openlca.ilcd.lists;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LocationType", namespace = "http://lca.jrc.it/ILCD/Locations", propOrder = {
		"value"
})
public class Location implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlValue
	public String value;

	@XmlAttribute(name = "value", required = true)
	public String locationCode;

}
