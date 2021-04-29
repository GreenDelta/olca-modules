package org.openlca.ilcd.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "dataEntry", "publication" })
public class AdminInfo {

	@XmlElement(name = "dataEntryBy")
	public DataEntry dataEntry;

	@XmlElement(name = "publicationAndOwnership")
	public Publication publication;

}
