package org.openlca.ilcd.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "dataEntry", "publication" })
public class AdminInfo {

	@XmlElement(name = "dataEntryBy")
	public DataEntry dataEntry;

	@XmlElement(name = "publicationAndOwnership")
	public Publication publication;

}
