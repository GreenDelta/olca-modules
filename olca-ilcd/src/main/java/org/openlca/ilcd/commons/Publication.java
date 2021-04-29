
package org.openlca.ilcd.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PublicationAndOwnershipType", propOrder = {
		"version",
		"precedingVersions",
		"uri",
		"owner",
		"other"
})
public class Publication implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", required = true, name = "dataSetVersion")
	public String version;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToPrecedingDataSetVersion")
	public final List<Ref> precedingVersions = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "permanentDataSetURI")
	@XmlSchemaType(name = "anyURI")
	public String uri;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToOwnershipOfDataSet")
	public Ref owner;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
