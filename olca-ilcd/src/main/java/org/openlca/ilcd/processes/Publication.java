
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

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.LangString;
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
	public final List<Ref> precedingVersions = new ArrayList<>();

	@XmlSchemaType(name = "anyURI")
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "permanentDataSetURI")
	public String uri;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "workflowAndPublicationStatus")
	public PublicationStatus status;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToUnchangedRepublication")
	public Ref republication;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToRegistrationAuthority")
	public Ref registrationAuthority;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public String registrationNumber;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToOwnershipOfDataSet")
	public Ref owner;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Boolean copyright;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToEntitiesWithExclusiveAccess")
	public final List<Ref> entitiesWithExclusiveAccess = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "licenseType")
	public LicenseType license;

	@FreeText
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<LangString> accessRestrictions = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public Publication clone() {
		Publication clone = new Publication();
		clone.lastRevision = lastRevision;
		clone.version = version;
		Ref.copy(precedingVersions, clone.precedingVersions);
		clone.uri = uri;
		clone.status = status;
		if (republication != null)
			clone.republication = republication.clone();
		if (registrationAuthority != null)
			clone.registrationAuthority = registrationAuthority.clone();
		clone.registrationNumber = registrationNumber;
		if (owner != null)
			clone.owner = owner.clone();
		clone.copyright = copyright;
		Ref.copy(entitiesWithExclusiveAccess, clone.entitiesWithExclusiveAccess);
		clone.license = license;
		LangString.copy(accessRestrictions, clone.accessRestrictions);
		if (other != null)
			clone.other = other.clone();
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}

}
