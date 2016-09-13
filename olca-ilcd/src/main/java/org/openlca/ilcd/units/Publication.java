
package org.openlca.ilcd.units;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.Other;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PublicationAndOwnershipType", propOrder = {
		"dataSetVersion",
		"referenceToPrecedingDataSetVersion",
		"permanentDataSetURI",
		"referenceToOwnershipOfDataSet",
		"other"
})
public class Publication implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", required = true)
	public String dataSetVersion;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<DataSetReference> referenceToPrecedingDataSetVersion = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	@XmlSchemaType(name = "anyURI")
	public String permanentDataSetURI;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public DataSetReference referenceToOwnershipOfDataSet;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
