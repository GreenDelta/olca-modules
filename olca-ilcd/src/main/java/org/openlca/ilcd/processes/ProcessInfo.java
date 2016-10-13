
package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Time;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessInformationType", propOrder = {
		"dataSetInfo",
		"quantitativeReference",
		"time",
		"geography",
		"technology",
		"parameters",
		"other"
})
public class ProcessInfo implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(required = true, name = "dataSetInformation")
	public DataSetInfo dataSetInfo;

	public QuantitativeReference quantitativeReference;

	public Time time;

	public Geography geography;

	public Technology technology;

	@XmlElement(name = "mathematicalRelations")
	public ParameterSection parameters;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
