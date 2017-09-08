package org.openlca.ilcd.models;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.Ref;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
		"timeStamp",
		"formats",
})
public class DataEntry {

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public XMLGregorianCalendar timeStamp;

	/** Describes the format of the data set. */
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToDataSetFormat")
	public final List<Ref> formats = new ArrayList<>();

}
