
package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.LangString;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"name",
		"defaultUnit",
		"reference"
})
@XmlRootElement(name = "referenceFlowProperty", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Flow")
public class ReferenceFlowProperty implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Flow")
	public final List<LangString> name = new ArrayList<>();

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Flow", required = true)
	public String defaultUnit;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public DataSetReference reference;

	@XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
	@XmlSchemaType(name = "anyURI")
	public String href;

}
