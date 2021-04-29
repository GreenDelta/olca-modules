package org.openlca.ilcd.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
		"version",
		"uri",
		"copyright"
})
public class Publication {

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "dataSetVersion")
	public String version;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "permanentDataSetURI")
	public String uri;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Boolean copyright;

}
