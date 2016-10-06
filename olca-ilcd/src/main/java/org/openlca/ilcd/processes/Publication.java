
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
		"dateOfLastRevision",
		"dataSetVersion",
		"referenceToPrecedingDataSetVersion",
		"permanentDataSetURI",
		"workflowAndPublicationStatus",
		"referenceToUnchangedRepublication",
		"referenceToRegistrationAuthority",
		"registrationNumber",
		"referenceToOwnershipOfDataSet",
		"copyright",
		"referenceToEntitiesWithExclusiveAccess",
		"licenseType",
		"accessRestrictions",
		"other"
})
public class Publication implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public XMLGregorianCalendar dateOfLastRevision;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", required = true)
	public String dataSetVersion;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<DataSetReference> referenceToPrecedingDataSetVersion = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	@XmlSchemaType(name = "anyURI")
	public String permanentDataSetURI;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public PublicationStatus workflowAndPublicationStatus;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public DataSetReference referenceToUnchangedRepublication;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public DataSetReference referenceToRegistrationAuthority;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public String registrationNumber;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public DataSetReference referenceToOwnershipOfDataSet;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Boolean copyright;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<DataSetReference> referenceToEntitiesWithExclusiveAccess = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public LicenseType licenseType;

	@FreeText
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<LangString> accessRestrictions = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
