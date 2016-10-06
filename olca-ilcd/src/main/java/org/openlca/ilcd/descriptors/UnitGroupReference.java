
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
@XmlType(name = "", propOrder = {
		"name",
		"defaultUnit",
		"reference"
})
public class UnitGroupReference implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/FlowProperty", required = true)
	public LangString name;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/FlowProperty", required = true)
	public String defaultUnit;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public DataSetReference reference;

	@XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
	@XmlSchemaType(name = "anyURI")
	public String href;

}
