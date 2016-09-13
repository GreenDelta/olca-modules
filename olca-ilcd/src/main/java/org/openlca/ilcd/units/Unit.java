
package org.openlca.ilcd.units;

import java.io.Serializable;
import java.math.BigInteger;
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

import org.openlca.ilcd.commons.Label;
import org.openlca.ilcd.commons.Other;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UnitType", propOrder = {
		"name",
		"meanValue",
		"generalComment",
		"other"
})
public class Unit implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(required = true)
	public String name;

	public double meanValue;

	public final List<Label> generalComment = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAttribute(name = "dataSetInternalID")
	public BigInteger dataSetInternalID;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
