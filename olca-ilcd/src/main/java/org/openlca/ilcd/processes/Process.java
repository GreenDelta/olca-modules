package org.openlca.ilcd.processes;

import java.util.ArrayList;
import java.util.Collections;
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

import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessDataSetType", propOrder = { "processInfo",
		"modelling", "adminInfo", "exchanges",
		"lciaResults", "other" })
public class Process implements IDataSet {

	private final static long serialVersionUID = 1L;

	@XmlElement(required = true, name = "processInformation")
	public ProcessInfo processInfo;

	@XmlElement(name = "modellingAndValidation")
	public Modelling modelling;

	@XmlElement(name = "administrativeInformation")
	public AdminInfo adminInfo;

	@XmlElementWrapper(name = "exchanges")
	@XmlElement(name = "exchange")
	public final List<Exchange> exchanges = new ArrayList<>();

	@XmlElementWrapper(name = "LCIAResults")
	@XmlElement(name = "LCIAResult")
	public LCIAResult[] lciaResults;

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

	@Override
	public DataSetType getDataSetType() {
		return DataSetType.PROCESS;
	}

	@Override
	public String getURI() {
		if (adminInfo == null || adminInfo.publication == null)
			return null;
		return adminInfo.publication.uri;
	}

	@Override
	public String getUUID() {
		if (processInfo == null || processInfo.dataSetInfo == null)
			return null;
		return processInfo.dataSetInfo.uuid;
	}

	@Override
	public String getVersion() {
		if (adminInfo == null || adminInfo.publication == null)
			return null;
		return adminInfo.publication.version;
	}

	@Override
	public List<Classification> getClassifications() {
		if (processInfo == null || processInfo.dataSetInfo == null)
			return Collections.emptyList();
		return processInfo.dataSetInfo.classifications;
	}

	@Override
	public List<LangString> getName() {
		if (processInfo == null || processInfo.dataSetInfo == null)
			return Collections.emptyList();
		ProcessName name = processInfo.dataSetInfo.name;
		if (name == null)
			return Collections.emptyList();
		return name.name;
	}

	@Override
	public Process clone() {
		Process clone = new Process();
		if (processInfo != null)
			clone.processInfo = processInfo.clone();
		if (modelling != null)
			clone.modelling = modelling.clone();
		if (adminInfo != null)
			clone.adminInfo = adminInfo.clone();
		for (Exchange e : exchanges) {
			if (e == null)
				continue;
			clone.exchanges.add(e.clone());
		}
		cloneResults(clone);
		if (other != null)
			clone.other = other.clone();
		clone.version = version;
		clone.locations = locations;
		clone.metaDataOnly = metaDataOnly;
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}

	private void cloneResults(Process clone) {
		if (lciaResults == null)
			return;
		clone.lciaResults = new LCIAResult[lciaResults.length];
		for (int i = 0; i < lciaResults.length; i++) {
			if (lciaResults[i] == null)
				continue;
			clone.lciaResults[i] = lciaResults[i].clone();
		}
	}

	public void add(LCIAResult r) {
		if (r == null)
			return;
		if (lciaResults == null) {
			lciaResults = new LCIAResult[] { r };
			return;
		}
		LCIAResult[] next = new LCIAResult[lciaResults.length + 1];
		System.arraycopy(lciaResults, 0, next, 0, lciaResults.length);
		next[lciaResults.length] = r;
		lciaResults = next;
	}
}
