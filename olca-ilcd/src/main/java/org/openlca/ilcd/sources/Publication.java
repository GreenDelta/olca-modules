
package org.openlca.ilcd.sources;

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

/**
 * <p>
 * Java class for PublicationAndOwnershipType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="PublicationAndOwnershipType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://lca.jrc.it/ILCD/Common}PublicationAndOwnershipGroup1"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}referenceToOwnershipOfDataSet" minOccurs="0"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}other" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
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
