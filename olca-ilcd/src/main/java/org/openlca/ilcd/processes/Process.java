package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Other;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessDataSetType", propOrder = { "processInformation",
		"modellingAndValidation", "administrativeInformation", "exchanges",
		"lciaResults", "other" })
public class Process implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(required = true)
	public ProcessInfo processInformation;

	public ModellingAndValidation modellingAndValidation;

	public AdminInfo administrativeInformation;

	@XmlElementWrapper(name = "exchanges")
	@XmlElement(name = "exchange")
	public final List<Exchange> exchanges = new ArrayList<>();

	@XmlElementWrapper(name = "LCIAResults")
	@XmlElement(name = "LCIAResult")
	public final List<LCIAResult> lciaResults = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAttribute(name = "version", required = true)
	public String version;

	@XmlAttribute(name = "locations")
	public String locations;

	@XmlAttribute(name = "metaDataOnly")
	public Boolean metaDataOnly;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
