
package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.Ref;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataEntryByType", propOrder = {
		"timeStamp",
		"formats",
		"originalDataSet",
		"documentor",
		"recommendationBy"
})
public class DataEntry implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public XMLGregorianCalendar timeStamp;

	@XmlElement(name = "referenceToDataSetFormat", namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<Ref> formats = new ArrayList<>();

	@XmlElement(name = "referenceToConvertedOriginalDataSetFrom", namespace = "http://lca.jrc.it/ILCD/Common")
	public Ref originalDataSet;

	@XmlElement(name = "referenceToPersonOrEntityEnteringTheData", namespace = "http://lca.jrc.it/ILCD/Common")
	public Ref documentor;

	public Recommendation recommendationBy;

}
