
package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LocationType", propOrder = {
		"code"
})
public class Location implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlValue
	public String code;

	@XmlAttribute(name = "latitudeAndLongitude")
	public String latLong;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
