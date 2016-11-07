
package org.openlca.ilcd.methods;

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
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.PublicationStatus;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.annotations.FreeText;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PublicationAndOwnershipType", propOrder = {
		"lastRevision",
		"version",
		"precedingVersions",
		"uri",
		"publicationStatus",
		"republication",
		"owner",
		"copyright",
		"accessRestrictions",
		"other"
})
public class Publication implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "dateOfLastRevision", namespace = "http://lca.jrc.it/ILCD/Common")
	public XMLGregorianCalendar lastRevision;

	@XmlElement(name = "dataSetVersion", namespace = "http://lca.jrc.it/ILCD/Common", required = true)
	public String version;

	@XmlElement(name = "referenceToPrecedingDataSetVersion", namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<Ref> precedingVersions = new ArrayList<>();

	@XmlElement(name = "permanentDataSetURI", namespace = "http://lca.jrc.it/ILCD/Common")
	@XmlSchemaType(name = "anyURI")
	public String uri;

	@XmlElement(name = "workflowAndPublicationStatus", namespace = "http://lca.jrc.it/ILCD/Common")
	public PublicationStatus publicationStatus;

	@XmlElement(name = "referenceToUnchangedRepublication", namespace = "http://lca.jrc.it/ILCD/Common")
	public Ref republication;

	@XmlElement(name = "referenceToOwnershipOfDataSet", namespace = "http://lca.jrc.it/ILCD/Common")
	public Ref owner;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Boolean copyright;

	@FreeText
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<LangString> accessRestrictions = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
