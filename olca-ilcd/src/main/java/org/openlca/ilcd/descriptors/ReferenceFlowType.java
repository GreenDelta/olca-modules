
package org.openlca.ilcd.descriptors;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.LangString;

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
