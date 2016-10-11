
package org.openlca.ilcd.processes;

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

import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.LicenseType;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.PublicationStatus;
import org.openlca.ilcd.commons.annotations.FreeText;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PublicationAndOwnershipType", propOrder = {
		"lastRevision",
		"version",
		"precedingVersions",
		"uri",
		"status",
		"republication",
		"registrationAuthority",
		"registrationNumber",
		"owner",
		"copyright",
		"entitiesWithExclusiveAccess",
		"license",
		"accessRestrictions",
		"other"
})
public class Publication implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "dateOfLastRevision")
	public XMLGregorianCalendar lastRevision;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", required = true, name = "dataSetVersion")
	public String version;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToPrecedingDataSetVersion")
	public final List<DataSetReference> precedingVersions = new ArrayList<>();

	@XmlSchemaType(name = "anyURI")
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "permanentDataSetURI")
	public String uri;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "workflowAndPublicationStatus")
	public PublicationStatus status;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToUnchangedRepublication")
	public DataSetReference republication;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToRegistrationAuthority")
	public DataSetReference registrationAuthority;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public String registrationNumber;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToOwnershipOfDataSet")
	public DataSetReference owner;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Boolean copyright;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToEntitiesWithExclusiveAccess")
	public final List<DataSetReference> entitiesWithExclusiveAccess = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "licenseType")
	public LicenseType license;

	@FreeText
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<LangString> accessRestrictions = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
