
package org.openlca.ilcd.descriptors;

import java.io.Serializable;

import org.openlca.ilcd.commons.LangString;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReferenceFlowType", propOrder = {
		"name",
		"flowProperty",
		"unit",
		"meanValue",
		"reference"
})
public class ReferenceFlowType implements Serializable {

	private final static long serialVersionUID = 1L;

	public LangString name;

	public LangString flowProperty;

	public String unit;

	public Double meanValue;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public DataSetReference reference;

	@XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
	@XmlSchemaType(name = "anyURI")
	public String href;

	@XmlAttribute(name = "internalId")
	public Integer internalId;

}
