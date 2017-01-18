package org.openlca.ilcd.descriptors;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.FlowCompleteness;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.ProcessType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"uuid",
		"uri",
		"version",
		"name",
		"classification",
		"comment",
		"synonyms",
		"type",
		"location",
		"time",
		"parameterized",
		"hasResults",
		"lciMethodInformation",
		"completenessProductModel",
		"complianceSystem",
		"review",
		"overallQuality",
		"useAdvice",
		"technicalPurpose",
		"accessInformation",
		"format",
		"ownership",
		"approvedBy" })
public class ProcessDescriptor extends Descriptor {

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public final List<LangString> synonyms = new ArrayList<>();

	public ProcessType type;

	public String location;

	public Time time;

	public Boolean parameterized;

	public Boolean hasResults;

	public LciMethodInformation lciMethodInformation;

	public FlowCompleteness completenessProductModel;

	public final List<ComplianceSystem> complianceSystem = new ArrayList<>();

	public Review review;

	public String overallQuality;

	public LangString useAdvice;

	public String technicalPurpose;

	public AccessInfo accessInformation;

	public String format;

	public DataSetReference ownership;

	public DataSetReference approvedBy;

	@XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
	@XmlSchemaType(name = "anyURI")
	public String href;

	@XmlAttribute(name = "sourceId", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public String sourceId;

	@XmlAttribute(name = "accessRestricted", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public Boolean accessRestricted;

	@Override
	protected DataSetType getType() {
		return DataSetType.PROCESS;
	}
}
