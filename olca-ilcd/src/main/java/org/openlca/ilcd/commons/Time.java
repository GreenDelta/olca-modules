package org.openlca.ilcd.commons;

import java.io.Serializable;
import java.math.BigInteger;
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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TimeType", propOrder = { "referenceYear", "validUntil",
		"description", "other" })
public class Time implements Serializable {

	private final static long serialVersionUID = 1L;

	public BigInteger referenceYear;

	@XmlElement(name = "dataSetValidUntil")
	public BigInteger validUntil;

	@XmlElement(name = "timeRepresentativenessDescription")
	public final List<FreeText> description = new ArrayList<>();

	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
