
package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Other;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LCIAMethodDataSetType", propOrder = {
		"methodInfo",
		"modelling",
		"adminInfo",
		"characterisationFactors",
		"other"
})
public class LCIAMethod implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "LCIAMethodInformation", required = true)
	public MethodInfo methodInfo;

	@XmlElement(name = "modellingAndValidation", required = true)
	public Modelling modelling;

	@XmlElement(name = "administrativeInformation")
	public AdminInfo adminInfo;

	public FactorList characterisationFactors;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAttribute(name = "version", required = true)
	public String version;

	@XmlAttribute(name = "locations")
	public String locations;

	@XmlAttribute(name = "LCIAMethodologies")
	public String lciaMethodologies;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
