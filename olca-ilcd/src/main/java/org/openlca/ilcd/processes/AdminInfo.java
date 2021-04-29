package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.commons.Other;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AdministrativeInformationType", propOrder = {
		"commissionerAndGoal",
		"dataGenerator",
		"dataEntry",
		"publication",
		"other"
})
public class AdminInfo implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public CommissionerAndGoal commissionerAndGoal;

	public DataGenerator dataGenerator;

	@XmlElement(name = "dataEntryBy")
	public DataEntry dataEntry;

	@XmlElement(name = "publicationAndOwnership")
	public Publication publication;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public AdminInfo clone() {
		AdminInfo clone = new AdminInfo();
		if (commissionerAndGoal != null)
			clone.commissionerAndGoal = commissionerAndGoal.clone();
		if (dataGenerator != null)
			clone.dataGenerator = dataGenerator.clone();
		if (dataEntry != null)
			clone.dataEntry = dataEntry.clone();
		if (publication != null)
			clone.publication = publication.clone();
		if (other != null)
			clone.other = other.clone();
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}
}
