
package org.openlca.ilcd.contacts;

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
@XmlType(name = "ContactDataSetType", propOrder = {
		"contactInformation",
		"administrativeInformation",
		"other"
})
public class Contact implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(required = true)
	public ContactInfo contactInformation;

	public AdminInfo administrativeInformation;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAttribute(name = "version", required = true)
	public String version;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
