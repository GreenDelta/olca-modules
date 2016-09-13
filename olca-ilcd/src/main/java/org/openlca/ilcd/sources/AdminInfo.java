
package org.openlca.ilcd.sources;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Other;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AdministrativeInformationType", propOrder = {
		"dataEntryBy",
		"publicationAndOwnership",
		"other"
})
public class AdminInfo implements Serializable {

	private final static long serialVersionUID = 1L;

	public DataEntry dataEntryBy;

	public Publication publicationAndOwnership;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
