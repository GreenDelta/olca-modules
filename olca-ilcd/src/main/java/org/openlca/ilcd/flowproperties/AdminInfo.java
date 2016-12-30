
package org.openlca.ilcd.flowproperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Publication;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AdministrativeInformationType", propOrder = {
		"dataEntry",
		"publication",
		"other"
})
public class AdminInfo implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "dataEntryBy")
	public DataEntry dataEntry;

	@XmlElement(name = "publicationAndOwnership")
	public Publication publication;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
