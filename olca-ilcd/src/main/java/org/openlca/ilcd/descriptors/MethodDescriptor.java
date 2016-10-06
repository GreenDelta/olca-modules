
package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.LangString;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"uuid",
		"permanentUri",
		"dataSetVersion",
		"name",
		"classification",
		"generalComment"
})
public class MethodDescriptor implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public String uuid;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	@XmlSchemaType(name = "anyURI")
	public String permanentUri;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public String dataSetVersion;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public LangString name;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public final List<Classification> classification = new ArrayList<>();

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public LangString generalComment;

	@XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
	@XmlSchemaType(name = "anyURI")
	public String href;

	@XmlAttribute(name = "sourceId", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public String sourceId;

}
