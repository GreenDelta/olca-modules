
package org.openlca.ilcd.flows;

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
@XmlType(name = "FlowDataSetType", propOrder = {
		"flowInformation",
		"modellingAndValidation",
		"administrativeInformation",
		"flowProperties",
		"other"
})
public class Flow implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(required = true)
	public FlowInfo flowInformation;

	public ModellingAndValidation modellingAndValidation;

	public AdminInfo administrativeInformation;

	public FlowPropertyList flowProperties;

	@XmlAttribute(name = "version", required = true)
	public String version;

	@XmlAttribute(name = "locations")
	public String locations;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public Map<QName, String> otherAttributes = new HashMap<>();

}
