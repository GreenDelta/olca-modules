package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Ref;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataEntryByType", propOrder = {
		"timeStamp",
		"formats",
		"originalDataSet",
		"documentor",
		"useApprovals",
		"other"
})
public class DataEntry implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public XMLGregorianCalendar timeStamp;

	/** Describes the format of the data set. */
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToDataSetFormat")
	public final List<Ref> formats = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToConvertedOriginalDataSetFrom")
	public Ref originalDataSet;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToPersonOrEntityEnteringTheData")
	public Ref documentor;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToDataSetUseApproval")
	public final List<Ref> useApprovals = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public DataEntry clone() {
		DataEntry clone = new DataEntry();
		clone.timeStamp = timeStamp;
		Ref.copy(formats, clone.formats);
		if (originalDataSet != null)
			clone.originalDataSet = originalDataSet.clone();
		if (documentor != null)
			clone.documentor = documentor.clone();
		Ref.copy(useApprovals, clone.useApprovals);
		if (other != null)
			clone.other = other.clone();
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}

}
