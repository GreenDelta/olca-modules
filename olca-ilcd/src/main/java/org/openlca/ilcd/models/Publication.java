package org.openlca.ilcd.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
