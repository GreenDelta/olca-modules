package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;

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
