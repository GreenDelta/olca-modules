
package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalReferenceType", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI", propOrder = {
		"shortDescription"
})
public class DataSetReference implements Serializable {

	private final static long serialVersionUID = 1L;

	public final List<LangString> shortDescription = new ArrayList<>();

	@XmlAttribute(name = "type", required = true)
	public DataSetType type;

	@XmlAttribute(name = "refObjectId")
	public String refObjectId;

	@XmlAttribute(name = "version")
	public String version;

	@XmlAttribute(name = "uri")
	@XmlSchemaType(name = "anyURI")
	public String uri;

	@XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
	@XmlSchemaType(name = "anyURI")
	public String href;

}
